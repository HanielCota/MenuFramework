# MenuFramework: guide for code-generating agents

This file is the single, complete reference for generating a plugin that uses MenuFramework. It is
written for AI coding assistants. Everything here is verified against the source. If a parameter,
key, or rule is not listed here, it does not exist. Do not invent API.

## Mental model

- **Behaviour lives in a Java class** annotated with `@Menu`. Methods annotated with `@Button`,
  `@Paginated`, `@Tick`, `@OnOpen`, `@OnClose` define behaviour.
- **Appearance lives in YAML** at `plugins/<PluginName>/menus/<id>.yml`, where `<id>` is the
  `@Menu(id)`. The framework merges the class and the YAML at boot.
- **Two kinds of menu:**
  - **Static**: `@Menu` + `@Button` methods. No pagination.
  - **Paginated**: has one `@Paginated` method returning `List<MenuItem>`. Only paginated menus
    support `@Reactive` state, `@Tick`, `@OnOpen`, and `@OnClose`.

## Setup

Dependency (JitPack, Gradle Kotlin DSL):

```kotlin
repositories {
    maven("https://jitpack.io")
}
dependencies {
    compileOnly("com.github.HanielCota.MenuFramework:menu-paper:v0.2.0")
    // For Folia support also add:
    // compileOnly("com.github.HanielCota.MenuFramework:menu-folia:v0.2.0")
}
```

Bootstrap once in `onEnable` and open menus by id or class:

```java
MenuFramework framework =
    MenuFramework.builder(this)
        .scan("com.example.plugin.menu") // package(s) containing @Menu classes
        .build();

framework.open(player, new MenuId("main"));   // by id
framework.open(player, MainMenu.class);        // by class
```

To act on the menu a player already has open — e.g. to refresh it when a domain event changes data it
reads — ask the framework for the session (it reads the live open inventory, so there is no view
registry to maintain):

```java
framework.session(player).ifPresent(session -> {
  session.menuId();   // which menu is open (a MenuId)
  session.refresh();  // re-render its dynamic content (re-runs @Paginated; no-op for static menus)
  session.close();    // close it for the player
});
// also: framework.session(playerId) — empty if the player is offline
```

`MenuSession` is a transient snapshot — query it again rather than caching it. `refresh()` is for data
the menu reads but does not own; state the menu owns should still change through a `@Reactive` field.

If menus need constructor dependencies, pass an instantiator:

```java
MenuFramework.builder(this)
    .instantiator(type -> container.create(type))
    .scan("com.example.plugin.menu")
    .build();
```

When a button action throws, the framework cancels the click and logs the failure with its full
stacktrace. To react in your own way (message the viewer, report to an error tracker), register a
handler — it replaces the default logging:

```java
MenuFramework.builder(this)
    .onActionError((viewer, failure) ->
        viewer.sendMessage(Component.text("Something went wrong.", NamedTextColor.RED)))
    .scan("com.example.plugin.menu")
    .build();
```

The handler runs on the view's owning thread (so it may touch the Bukkit API) and receives the
`Player` and the thrown `RuntimeException`. A handler that itself throws is logged and swallowed,
never escaping into Bukkit's event pipeline.

Tear the framework down in `onDisable` — it unregisters the listeners, cancels tick tasks, closes
open menus and clears the registry:

```java
@Override
public void onDisable() {
  framework.shutdown();
}
```

### `MenuFramework` facade methods

| Method | Returns | Use |
|--------|---------|-----|
| `scan(String... basePackages)` | `MenuFramework` | Discover and register every `@Menu` under the packages. |
| `register(Object menu)` | `MenuFramework` | Manually register one already-constructed `@Menu` instance. |
| `open(Player, MenuId)` / `open(Player, Class<?>)` | `void` | Open a registered menu for a player (no-op without permission or if unregistered). |
| `session(Player)` / `session(PlayerId)` | `Optional<MenuSession>` | Handle to the player's currently open framework menu. |
| `reload(MenuId)` | `boolean` | Reload one menu from YAML; `true` if it existed. |
| `reloadReport(MenuId)` | `ReloadReport` | Reload one menu and report success/failure. |
| `reloadAll()` | `int` | Reload every menu; returns the count reloaded. |
| `reloadAllReport()` | `ReloadReport` | Reload every menu with a success/failure report. |
| `reloadAllReportAsync()` | `CompletableFuture<ReloadReport>` | Same, with YAML IO off the main thread. |
| `close(Player)` | `void` | Close the player's open inventory. |
| `shutdown()` | `void` | Unregister listeners, cancel tasks, close menus, clear the registry. Call in `onDisable`. |

`ReloadReport` exposes `successful()`, `successCount()`, `reloaded()` (a `List<MenuId>`) and
`failures()` (a `List<ReloadFailure>`, each with `id()` and `message()`).

### `MenuFramework.builder(plugin)` methods

| Builder method | Use |
|----------------|-----|
| `scan(String... packages)` | Packages to scan for `@Menu` classes during `build()`. |
| `instantiator(MenuInstanceFactory)` | Custom construction (e.g. a DI container) for scanned menu classes. |
| `menusDirectory(Path)` | Override the YAML directory (default `plugins/<PluginName>/menus`). |
| `scheduler(MenuScheduler)` | Override platform scheduler auto-detection (Paper vs Folia). |
| `onActionError(MenuErrorHandler)` | Replace the default logging of a throwing button action. |
| `build()` | Wire the framework, register listeners once, scan configured packages. Call once. |

## Annotations

| Annotation | Target | Parameters (with defaults) | Rules |
|-----------|--------|----------------------------|-------|
| `@Menu` | class | `id` (required), `permission` (default `""`) | `id` must match `[a-z0-9_-]+`, max 64 chars, and equal the YAML file name. |
| `@Button` | method | `id` (required), `permission` (default `""`), `cooldownMillis` (default `0`) | `id` must match a key under `buttons` in the YAML. Takes 0 or 1 parameter (see below). |
| `@Paginated` | method | none | Exactly one per paginated menu. Must take no args and return `List<MenuItem>`. |
| `@Reactive` | field | none | Field type must be `State<?>`. Paginated menus only. |
| `@Viewer` | field | none | Field type must be a non-final, non-static `PlayerId`. The viewer is injected before the first render, so `@Paginated`/`@Button` methods can read it. Paginated menus only. |
| `@Arg` | field | none | Field type must be a non-final, non-static reference type (never a primitive). The open argument passed to `open(player, id, argument)` is injected into every `@Arg` field it is assignable to, before the first render. Paginated menus only. |
| `@Tick` | method | `period` (ticks, default `20`) | Must take no args and return `void`. `period` must be >= 1. Paginated menus only. |
| `@OnOpen` | method | none | Takes no args or a single `Player`. Paginated menus only. |
| `@OnClose` | method | none | Takes no args or a single `Player`. Runs after state and ticks are torn down. Paginated menus only. |
| `@RefreshOn` | class | `value` (one or more `Class<? extends Event>`, required) | Re-renders the open view when any listed Bukkit event fires. Paper-layer annotation (`dev.haniel.menu.paper.annotation.RefreshOn`). Paginated menus only. |

### `@Button` method parameters

A `@Button` method takes **zero or one** parameter. The allowed parameter types are exactly:

| Parameter type | Import | Use |
|----------------|--------|-----|
| (none) | | Fire-and-forget action. |
| `MenuClick` | `dev.haniel.menu.paper.api.MenuClick` | Recommended. Convenience over the raw context. |
| `Player` | `org.bukkit.entity.Player` | The clicking player (never null; the click is skipped if they logged off). |
| `ClickContext` | `dev.haniel.menu.click.ClickContext` | The raw platform-neutral context. |

No other parameter types are supported. Two or more parameters is a boot error.

### `MenuClick` API

```java
Player player();          // the clicking player
PlayerId playerId();      // their id
ClickType clickType();    // LEFT, RIGHT, SHIFT_LEFT, SHIFT_RIGHT, MIDDLE, DROP,
                          // DOUBLE_CLICK, NUMBER_KEY, SWAP_OFFHAND, OTHER
void message(String miniMessageText); // sends a MiniMessage line to the player
void open(MenuId id);     // opens another registered menu for the player
void open(Class<?> menuType); // same, by the menu's class
void prompt(AnvilPrompt<?> prompt); // opens an anvil text input for the player
void close();             // closes the player's menu
```

### Anvil text input (`AnvilPrompt`)

Ask the player to type a value in an anvil instead of building a keypad menu:

```java
import dev.haniel.menu.paper.api.AnvilPrompt;

@Button(id = "set-amount")
public void setAmount(MenuClick click) {
  click.prompt(
      AnvilPrompt.numeric()
          .title("<gray>Enter an amount")
          .onConfirm(amount -> { /* amount is an int */ })
          .onCancel(() -> click.open(new MenuId("shop"))));
}
```

- `AnvilPrompt.text()` confirms the raw typed `String`; `AnvilPrompt.numeric()` confirms an `int`.
- `.title(String)` sets the anvil title (trusted MiniMessage); `.initialText(String)` pre-fills the
  text field. Both are optional and default to empty.
- Invalid numeric input (non-numeric or blank) leaves the anvil open for another try — `onConfirm`
  only fires on a valid value.
- `onCancel` runs when the player closes the anvil without confirming. The framework does not reopen
  the previous menu — do that yourself from `onConfirm`/`onCancel` with `click.open(...)`.
- Like `open(...)`, `prompt(...)` works on a `@Button`-injected `MenuClick`, not on `MenuClick.of(...)`.
- **Security:** the confirmed value is the player's raw input. Never pass it to `message(...)` or any
  MiniMessage deserialization without escaping it first (see the `message` warning above).

Use `open(...)` to navigate between menus from a button — no need to inject a reference to the
framework. Opening a menu the player may not see (missing permission) or that is not registered is a
silent no-op, matching `MenuFramework.open`. Navigation is only available on a `MenuClick` injected
into a `@Button` method; a `MenuClick.of(context)` built inside a code-built `MenuItem.onClick`
lambda can read the player, message and close, but `open(...)` there throws `IllegalStateException`.

To go back, open the parent menu explicitly: a "back" button is just `click.open(parentId)`. Tree and
wizard flows (e.g. select → confirm) each have a fixed, known predecessor, so there is no framework
back-stack — the parent is whatever menu you choose to open.

## Java API for items and state

### Icon (immutable, fluent)

Build icons in code with `Icons.of(Material)` (Paper) or `Icon.of("MATERIAL_NAME")` (core).

```java
import dev.haniel.menu.paper.api.Icons;
import dev.haniel.menu.item.ItemFlag;

Icon icon = Icons.of(Material.DIAMOND)
    .named("<aqua>Gem</aqua>")              // MiniMessage display name
    .describedBy(List.of("<gray>Lore</gray>")) // MiniMessage lore lines
    .amount(16)                             // 1..64
    .glowing()                              // enchantment glint
    .unbreakable()
    .modelData(1001)
    .hiding(ItemFlag.HIDE_ATTRIBUTES);
```

`ItemFlag` values: `HIDE_ENCHANTS`, `HIDE_ATTRIBUTES`, `HIDE_UNBREAKABLE`, `HIDE_DESTROYS`,
`HIDE_PLACED_ON`, `HIDE_DYE`, `HIDE_ARMOR_TRIM`, `HIDE_ADDITIONAL_TOOLTIP`.

`amount` must be in `1..64`. Building an Icon in code with an out-of-range amount throws. (In YAML an
out-of-range `amount` is clamped to 1 instead.)

#### Player heads

Build a `PLAYER_HEAD` icon with a skin — the returned `Icon` chains `named`/`describedBy`/etc. as
usual:

```java
Icons.head(player);          // OfflinePlayer — that player's skin
Icons.head(uuid);            // by UUID
Icons.headTexture(base64);   // a fixed custom skin (base64 textures value)
```

`head(player)`/`head(uuid)` resolve the skin from the server's profile cache (a recently-seen player
shows correctly; an unknown id shows the default head until the client resolves it). `headTexture(...)`
is fully local and always renders. Heads are only available in code (no YAML key yet).

### MenuItem (a paginated content entry)

```java
import dev.haniel.menu.item.MenuItem;

MenuItem item = MenuItem.of(icon).onClick(context -> {
  // context is a ClickContext
});
```

### State (reactive value)

```java
import dev.haniel.menu.state.State;

@Reactive private final State<Integer> count = State.of(0);

count.get();        // read
count.set(count.get() + 1); // write; schedules a coalesced, diff-based re-render of the open menu
```

`State.set` is the only thing that triggers a re-render. Mutating a field directly does not.

### Viewer (the opening player)

```java
import dev.haniel.menu.annotation.Viewer;
import dev.haniel.menu.domain.PlayerId;

@Viewer private PlayerId viewer; // non-final, non-static

@Paginated
public List<MenuItem> items() {
  return shop.offersFor(viewer).stream().map(this::item).toList(); // viewer is set before this runs
}
```

The framework writes the viewer into every `@Viewer` field of the fresh per-player instance **before
the first render**, so a `@Paginated` provider (and any `@Button`/`@Tick`) can read it directly.
Prefer this over smuggling the viewer through `@Reactive State<UUID>` — that leaves the viewer unknown
on the first render and forces a wasted re-render.

### Arg (a typed open argument)

Open a menu *for* a target, amount or any context with `open(player, id, argument)` and receive it in
an `@Arg` field — no hand-rolled session carrier between menus.

```java
import dev.haniel.menu.annotation.Arg;

@Arg private ProfileTarget target; // non-final, non-static reference type

@Paginated
public List<MenuItem> items() {
  return profiles.of(target).stream().map(this::item).toList(); // target is set before this runs
}
```

```java
// Application code or a @Button handler:
framework.open(viewer, new MenuId("profile"), new ProfileTarget(otherPlayerId));
```

The argument is injected into every `@Arg` field whose declared type it is assignable to, **before the
first render**. A menu may declare several `@Arg` fields of different types. Opening without an argument
leaves the fields at their defaults; opening with an argument that matches **no** `@Arg` field is a
runtime error, so a type mismatch fails loudly instead of leaving the field silently null.

### RefreshOn (re-render on a Bukkit event)

Re-render the open menu whenever a Bukkit event fires, so it reflects data it reads but does not own
(a balance changed elsewhere, an admin action) without a hand-written listener that calls `refresh()`.

```java
import dev.haniel.menu.paper.annotation.RefreshOn;

@Menu(id = "balance")
@RefreshOn(BalanceChangedEvent.class) // one or more event classes
public final class BalanceMenu {

  @Paginated
  public List<MenuItem> items() {
    return history.recent().stream().map(this::item).toList(); // re-runs when the event fires
  }
}
```

The subscription is registered while the view is open and removed on close, so there is nothing to
unregister by hand. It re-renders every open view of the menu regardless of which entity the event
concerns, on the thread the event fires on — pair it with main-thread domain events. For per-target
precision, call `framework.session(player).refresh()` from your own handler instead. Use `@Reactive`
for state the menu **owns**; use `@RefreshOn` for external changes it only **reads**. This is a
Paper-layer annotation (`dev.haniel.menu.paper.annotation.RefreshOn`); static menus reject it at boot.

## YAML reference

### Menu (top level)

| Key | Type | Default | Notes |
|-----|------|---------|-------|
| `title` | string (MiniMessage) | `""` | The inventory title. |
| `rows` | integer | (required) | Must be `1..6`. A missing/zero value is an error. |
| `buttons` | map of id to button | `{}` | Keys must match `@Button(id)`. |
| `pagination` | pagination object | absent | Present only for paginated menus. |

### Button (under `buttons`, and the nav buttons under `pagination`)

| Key | Type | Default | Notes |
|-----|------|---------|-------|
| `slot` | integer | `0` | Must be `0..rows*9-1`. Ignored for `previousButton`/`nextButton` (their slot comes from the mask). |
| `material` | string | `STONE` | A valid Bukkit `Material` name. Invalid names fail at boot. |
| `name` | string (MiniMessage) | `""` | Display name. |
| `lore` | list of string (MiniMessage) | `[]` | Lore lines. |
| `amount` | integer | `1` | Out-of-range (`<1` or `>64`) is clamped to `1`. |
| `glow` | boolean | `false` | Enchantment glint. |
| `unbreakable` | boolean | `false` | |
| `model-data` | integer | `0` | `0` or negative means none. |
| `flags` | list of `ItemFlag` | `[]` | See `ItemFlag` values above. |

### Pagination (under `pagination`)

| Key | Type | Notes |
|-----|------|-------|
| `mask` | list of strings | One string per row. Each string is 9 characters. The row count must equal `rows`. |
| `previous-button` | button object | Look of the previous-page control (slot comes from the mask `<`). |
| `next-button` | button object | Look of the next-page control (slot comes from the mask `>`). |

Note on key naming: YAML keys are kebab-case. Multi-word names map from the Java field, so
`previousButton` becomes `previous-button` and `modelData` becomes `model-data`.

Mask characters:

| Char | Meaning |
|:----:|---------|
| `X` | content slot (paginated items fill these in order) |
| `#` | border slot (filled with a gray pane, unless a button overlays it) |
| `<` | previous-page control |
| `>` | next-page control |
| (space) | empty slot |

The mask must contain at least one `X`. A `@Button` may sit on a `#` (border) slot and overrides the
pane, but never on an `X`, `<`, or `>` slot.

## Hard validation rules (boot errors if violated)

- `rows` must be `1..6`.
- `slot` must fit the inventory (`0..rows*9-1`).
- `material` must be a real Bukkit `Material`.
- Mask rows must equal `rows`, each row must be exactly 9 characters, and there must be at least one
  `X`.
- A `@Button(id)` must have a matching key under `buttons`, and vice versa for overlay buttons.
- `@Paginated` method: no args, returns `List<MenuItem>`. Only one per menu.
- `@Tick` method: no args, returns `void`, `period >= 1`.
- `@Reactive` field must be `State<?>`.
- `@Viewer` field must be a non-final, non-static `PlayerId`.
- Menu `id` must match `[a-z0-9_-]+` and be at most 64 characters.

Validation failures throw `InvalidMenuException` with a message naming the menu, at boot or reload.

## Threading rules

- `@Button`, `@Tick`, `@OnOpen`, `@OnClose` run on the view's owning thread (the main thread on
  Paper, the player's region thread on Folia). Do not block them with IO or database calls.
- For heavy work, go async yourself and hop back before touching Bukkit:
  `Bukkit.getScheduler().runTaskAsynchronously(...)` then `runTask(...)`. On Folia use the entity
  scheduler.
- Never store a `Player` or `Entity` in a field. Keep a `UUID`/`PlayerId` and resolve when needed.

## Security notes

- Clicks inside a menu are always cancelled; players cannot take or insert items.
- A click by a player lacking the button `permission` is silently ignored; `open` does nothing for a
  player lacking the menu `permission`.
- Placeholder values are MiniMessage-escaped before parsing, so player-controlled text (names,
  nicknames) cannot inject tags like `<click>` or `<hover>`. Author template tags still work.

## Complete templates

### 1. Static menu

`MainMenu.java`:

```java
package com.example.plugin.menu;

import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.paper.api.MenuClick;

@Menu(id = "main")
public final class MainMenu {

  @Button(id = "shop")
  public void openShop(MenuClick click) {
    click.message("<green>Opening the shop...</green>");
  }

  @Button(id = "close")
  public void close(MenuClick click) {
    click.close();
  }
}
```

`menus/main.yml`:

```yaml
title: "<green>Main Menu</green>"
rows: 3
buttons:
  shop:
    slot: 11
    material: EMERALD
    name: "<green>Shop</green>"
    lore:
      - "<gray>Click to browse.</gray>"
  close:
    slot: 15
    material: BARRIER
    name: "<red>Close</red>"
```

### 2. Paginated + reactive menu

`CatalogMenu.java`:

```java
package com.example.plugin.menu;

import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.Paginated;
import dev.haniel.menu.annotation.Reactive;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.paper.api.Icons;
import dev.haniel.menu.state.State;
import java.util.List;
import org.bukkit.Material;

@Menu(id = "catalog")
public final class CatalogMenu {

  @Reactive private final State<Boolean> showRare = State.of(false);

  @Button(id = "toggle-rare")
  public void toggleRare() {
    showRare.set(!showRare.get()); // re-renders the open view
  }

  @Paginated
  public List<MenuItem> items() {
    Material material = showRare.get() ? Material.DIAMOND : Material.IRON_INGOT;
    return List.of(
        MenuItem.of(Icons.of(material).named("<aqua>Item</aqua>")).onClick(context -> {}));
  }
}
```

`menus/catalog.yml`:

```yaml
title: "<gold>Catalog</gold>"
rows: 6
buttons:
  toggle-rare:
    slot: 49
    material: HOPPER
    name: "<yellow>Toggle rare</yellow>"
pagination:
  mask:
    - "#########"
    - "#XXXXXXX#"
    - "#XXXXXXX#"
    - "#XXXXXXX#"
    - "#XXXXXXX#"
    - "#<#####>#"
  previous-button:
    material: ARROW
    name: "<yellow>Previous</yellow>"
  next-button:
    material: ARROW
    name: "<yellow>Next</yellow>"
```

Note: `toggle-rare` sits on slot 49, which is a `#` (border) slot in the mask. That is allowed.

### 3. Countdown with `@Tick`

```java
@Menu(id = "event")
public final class EventMenu {

  @Reactive private final State<Integer> secondsLeft = State.of(300);

  @Tick(period = 20) // 20 ticks = 1 second
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

### 4. Permissions, cooldown, and lifecycle hooks

```java
@Menu(id = "vault", permission = "myplugin.vault.open")
public final class VaultMenu {

  @OnOpen
  public void onOpen(Player player) {
    player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1f, 1f);
  }

  @OnClose
  public void onClose(Player player) {
    player.playSound(player, Sound.BLOCK_CHEST_CLOSE, 1f, 1f);
  }

  @Button(id = "daily", permission = "myplugin.vault.daily", cooldownMillis = 86_400_000)
  public void claimDaily(MenuClick click) {
    click.message("<green>Daily reward claimed.</green>");
  }

  @Paginated
  public List<MenuItem> items() {
    return List.of();
  }
}
```

## Common mistakes and how the framework reports them

| Symptom / message | Cause | Fix |
|-------------------|-------|-----|
| `@Paginated method ... must take no args and return List<MenuItem>` | Provider has parameters or wrong return type. | Make it `public List<MenuItem> name()`. |
| `@Tick method ... must take no args and return void` | Tick method has args or a return type. | Make it `public void name()` and read state inside. |
| `Button '<id>' is annotated but missing in YAML` | A `@Button(id)` has no matching `buttons.<id>` key. | Add the button to the YAML, or remove the annotation. |
| `Unknown material: <name>` | Bad `material` in YAML or `Icon.of`. | Use a valid Bukkit `Material` enum name. |
| `Slot <n> is outside the menu bounds` / `slot must be >= 0` | `slot` is negative or beyond `rows*9-1`. | Pick a slot within the inventory. |
| `... is @Paginated but YAML has no 'pagination'` | Paginated class without a `pagination` section. | Add the `pagination` section with a `mask`. |
| `Button '<id>' cannot use content slot <n>` | A button overlays an `X` (content) or `<`/`>` (nav) slot. | Move the button to a `#` or empty slot. |
| Nothing happens on click | Player lacks the button `permission`, or is within `cooldownMillis`. | Grant the permission or wait out the cooldown. |
| Menu opens empty for a player | Player lacks the menu `permission`. | `open` is a no-op without it; grant the permission. |
| Reactive value does not update | The field was mutated directly instead of `state.set(...)`. | Always change reactive values through `State.set`. |
| Item amount throws at runtime | `Icon.amount(n)` called with `n` outside `1..64`. | Clamp the value before calling `amount`. |

## Do and do not

- Do put behaviour in the `@Menu` class and appearance in the YAML.
- Do return a fresh `List<MenuItem>` from `@Paginated`; it is called per render.
- Do use `state.set(...)` to update; do not mutate reactive fields directly.
- Do keep `@Tick`/`@Button` work cheap; offload IO to async and hop back to the main thread.
- Do not store `Player`/`Entity` in fields; keep `UUID`, or use a `@Viewer PlayerId` for the opener.
- Do not use legacy color codes (`&`, the section sign); use MiniMessage tags.
- Do not put `@Reactive`, `@Viewer`, `@Tick`, `@OnOpen`, or `@OnClose` on a static (non-paginated) menu.
