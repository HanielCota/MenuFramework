# MenuFramework

Annotation-driven inventory menu framework for Paper and Folia plugins.

Menu behavior lives in Java classes. Menu appearance lives in YAML files under
`plugins/<PluginName>/menus`. The framework compiles both at boot and can reload YAML at runtime.

## Modules

- `menu-core`: platform-free annotations, config, compiler, state and merge logic.
- `menu-paper`: Paper-facing facade, listener, rendering and registry.
- `menu-folia`: Folia scheduler implementation.
- `example-plugin`: runnable example plugin using the public API.

## Installation

The library modules are published through [JitPack](https://jitpack.io). Add the repository and the
modules you need (`menu-paper` brings in `menu-core` transitively):

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.<user>.MenuFramework:menu-paper:<tag>")
    // Add menu-folia as well when targeting Folia.
}
```

## Static Menu

```java
@Menu(id = "main")
public final class MainMenu {

  @Button(id = "open-catalog")
  public void openCatalog(MenuClick click) {
    click.message("<green>Opening catalog.</green>");
  }
}
```

```yaml
title: "<green>Main Menu</green>"
rows: 3
buttons:
  open-catalog:
    slot: 13
    material: CHEST
    name: "<green>Open Catalog</green>"
```

## Paginated Reactive Menu

```java
@Menu(id = "catalog")
public final class CatalogMenu {

  @Reactive private final State<Category> category = State.of(Category.TOOLS);

  @Button(id = "next-category")
  public void nextCategory() {
    category.set(category.get().next());
  }

  @Paginated
  public List<MenuItem> products() {
    return products.in(category.get()).stream().map(this::item).toList();
  }
}
```

The pagination mask uses:

- `X`: content slot
- `#`: border slot
- `<`: previous page
- `>`: next page
- space: empty slot

## Auto-Updating Menus (Countdowns & Animations)

A `@Tick` method runs on a fixed schedule while the menu is open, on the view's owning thread, so it
may update a `@Reactive State<?>` — which drives the usual coalesced, diff-based re-render. Use it for
a countdown in the lore or a frame-based animation. The tick starts on open and is cancelled on
close (no leaked task). `@Tick` works on paginated (reactive) menus.

```java
@Menu(id = "event")
public final class EventMenu {

  @Reactive private final State<Integer> secondsLeft = State.of(300);

  @Tick(period = 20) // every 20 ticks = 1 second
  public void countdown() {
    secondsLeft.set(Math.max(0, secondsLeft.get() - 1));
  }

  @Paginated
  public List<MenuItem> items() {
    return List.of(
        MenuItem.of(
            Icons.of(Material.CLOCK).named("<yellow>Starts in " + secondsLeft.get() + "s</yellow>")));
  }
}
```

## Placeholders (PlaceholderAPI)

Paginated menus resolve `%placeholders%` per viewer in the **page content** and the **title**, as a
PlaceholderAPI soft dependency: with PlaceholderAPI installed the tokens are filled for each player;
without it the text is left as-is. Resolution runs before MiniMessage parsing and only for the open
view, so each player sees their own values without cross-player cache bleed.

```yaml
title: "<gold>%player_name%'s Bag</gold>"
```

```java
@Paginated
public List<MenuItem> items() {
  return List.of(
      MenuItem.of(Icons.of(Material.GOLD_INGOT).named("<yellow>Balance: %vault_eco_balance%</yellow>")));
}
```

Placeholders that change over time (a balance, a timer) refresh on the next re-render — pair them
with `@Tick` for a live value. Static menus and a paginated menu's static overlay/navigation buttons
are shared across viewers and are not per-player resolved.

## Lifecycle Hooks

`@OnOpen` and `@OnClose` methods on a paginated menu run when the view opens and closes, on the
view's owning thread. Each takes no arguments or a single `Player` (the viewer). Reactive state and
ticks are torn down for you before `@OnClose` runs.

```java
@Menu(id = "shop")
public final class ShopMenu {

  @OnOpen
  public void onOpen(Player player) {
    player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1f, 1f);
  }

  @OnClose
  public void onClose(Player player) {
    player.playSound(player, Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
  }

  @Paginated
  public List<MenuItem> items() {
    return List.of();
  }
}
```

## Rich Items

Buttons and code-built icons carry an appearance beyond material, name and lore: stack `amount`, an
enchantment `glow`, `unbreakable`, custom `modelData` and tooltip `flags`.

In YAML:

```yaml
buttons:
  legendary:
    slot: 13
    material: NETHERITE_SWORD
    name: "<gold>Legendary Blade</gold>"
    amount: 1
    glow: true
    unbreakable: true
    modelData: 1001
    flags: [HIDE_ATTRIBUTES, HIDE_UNBREAKABLE]
```

In code (fluent and immutable; see `Icons`/`Icon`):

```java
Icon icon = Icons.of(Material.DIAMOND)
    .named("<aqua>Gem</aqua>")
    .amount(16)
    .glowing()
    .hiding(ItemFlag.HIDE_ATTRIBUTES);
```

## Permissions

Restrict who can open a menu or click a button. A click by a player lacking the permission is
silently ignored; `open` does nothing for a player lacking the menu permission.

```java
@Menu(id = "vault", permission = "myplugin.vault.open")
public final class VaultMenu {

  @Button(id = "withdraw", permission = "myplugin.vault.withdraw")
  public void withdraw(MenuClick click) {
    click.message("<green>Withdrawn.</green>");
  }
}
```

A button can also be rate-limited per player with `cooldownMillis`; a click made while cooling down
is silently dropped (permission is checked first, so a denied click never starts the cooldown):

```java
@Button(id = "daily", cooldownMillis = 3000)
public void claimDaily(MenuClick click) {
  click.message("<green>Claimed.</green>");
}
```

## Bootstrapping

```java
MenuFramework framework =
    MenuFramework.builder(this)
        .scan("com.example.plugin.menu")
        .build();
```

Use a custom instantiator when menus have constructor dependencies:

```java
MenuFramework.builder(this)
    .instantiator(type -> container.create(type))
    .scan("com.example.plugin.menu")
    .build();
```

## Reload

Synchronous compatibility API:

```java
boolean reloaded = framework.reload(new MenuId("main"));
int count = framework.reloadAll();
```

Detailed report API:

```java
ReloadReport report = framework.reloadAllReport();
```

Async YAML reload:

```java
framework.reloadAllReportAsync()
    .thenAccept(report -> sender.sendMessage(Component.text(report.successCount() + " reloaded")));
```

Only YAML IO and parsing run asynchronously. Menu compilation and `ItemStack` creation run on the
plugin scheduler because Bukkit inventory APIs are not thread-safe.

YAML reloads are cached by file metadata plus a CRC32 content checksum, so unchanged files are not
parsed again while timestamp edge cases still invalidate correctly.

## Validation

The loader validates platform-independent YAML errors early:

- `rows` must be between `1` and `6`
- button slots must fit the menu size
- pagination masks must match the menu rows and be 9 columns wide
- pagination masks must contain at least one `X`

Material names are validated in the Paper renderer with Bukkit's `Material` registry.

## Build

```powershell
.\gradlew.bat clean test build
```

The example plugin shadow jar is built at:

```text
example-plugin/build/libs/example-plugin-0.1.0-SNAPSHOT.jar
```

## License

Released under the [MIT License](LICENSE).
