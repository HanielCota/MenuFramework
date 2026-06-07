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
    compileOnly("com.github.HanielCota.MenuFramework:menu-paper:v0.1.0")
    // For Folia support also add:
    // compileOnly("com.github.HanielCota.MenuFramework:menu-folia:v0.1.0")
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

If menus need constructor dependencies, pass an instantiator:

```java
MenuFramework.builder(this)
    .instantiator(type -> container.create(type))
    .scan("com.example.plugin.menu")
    .build();
```

## Annotations

| Annotation | Target | Parameters (with defaults) | Rules |
|-----------|--------|----------------------------|-------|
| `@Menu` | class | `id` (required), `permission` (default `""`) | `id` must match `[a-z0-9_-]+`, max 64 chars, and equal the YAML file name. |
| `@Button` | method | `id` (required), `permission` (default `""`), `cooldownMillis` (default `0`) | `id` must match a key under `buttons` in the YAML. Takes 0 or 1 parameter (see below). |
| `@Paginated` | method | none | Exactly one per paginated menu. Must take no args and return `List<MenuItem>`. |
| `@Reactive` | field | none | Field type must be `State<?>`. Paginated menus only. |
| `@Tick` | method | `period` (ticks, default `20`) | Must take no args and return `void`. `period` must be >= 1. Paginated menus only. |
| `@OnOpen` | method | none | Takes no args or a single `Player`. Paginated menus only. |
| `@OnClose` | method | none | Takes no args or a single `Player`. Runs after state and ticks are torn down. Paginated menus only. |

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
void close();             // closes the player's menu
```

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
`HIDE_PLACED_ON`, `HIDE_DYE`, `HIDE_ARMOR_TRIM`.

`amount` must be in `1..64`. Building an Icon in code with an out-of-range amount throws. (In YAML an
out-of-range `amount` is clamped to 1 instead.)

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
- Do not store `Player`/`Entity` in fields; keep `UUID`.
- Do not use legacy color codes (`&`, the section sign); use MiniMessage tags.
- Do not put `@Reactive`, `@Tick`, `@OnOpen`, or `@OnClose` on a static (non-paginated) menu.
