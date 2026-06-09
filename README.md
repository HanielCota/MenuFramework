<div align="center">

# 🗃️ MenuFramework

### Annotation-driven inventory menus for **Paper** & **Folia**

Behaviour lives in Java. Appearance lives in YAML. Compiled at boot, hot-reloadable at runtime.

[![Build](https://github.com/HanielCota/MenuFramework/actions/workflows/build.yml/badge.svg)](https://github.com/HanielCota/MenuFramework/actions/workflows/build.yml)
[![JitPack](https://jitpack.io/v/HanielCota/MenuFramework.svg)](https://jitpack.io/#HanielCota/MenuFramework)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](#-build)
[![Platform](https://img.shields.io/badge/Paper%20%2F%20Folia-1.21%2B-brightgreen.svg)](#)

[Quick start](#-quick-start) · [Features](#-features) · [Guide](#-guide) · [Reload](#-reload) · [Build](#-build)

</div>

---

## 💡 Why MenuFramework

Building inventory GUIs by hand means juggling raw slot indices, cancelling click events, diffing
item stacks, and re-wiring everything on Folia. MenuFramework removes that boilerplate:

- 🎯 **Declarative:** annotate a class, drop a YAML file. No `InventoryClickEvent` plumbing.
- ⚡ **Reactive:** change a `State<?>` and the open menu re-renders by diff (only changed slots).
- 🧵 **Folia-ready:** the same code runs on Paper's main thread or a player's region thread.
- 🔁 **Hot-reload:** edit YAML and reload appearance at runtime, sync or async.
- 🛡️ **Safe by default:** clicks are cancelled, items can't be stolen, and player-controlled
  placeholders can't inject MiniMessage tags.
- 🧱 **Clean architecture:** a platform-free core, a thin Paper adapter, a Folia scheduler.

```java
@Menu(id = "main")
public final class MainMenu {

  @Button(id = "open-catalog")
  public void openCatalog(MenuClick click) {
    click.message("<green>Opening catalog…</green>");
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

That's a complete, clickable menu.

---

## 🚀 Quick start

### 1. Add the dependency

The library is published through [JitPack](https://jitpack.io/#HanielCota/MenuFramework).
`menu-paper` pulls in `menu-core` transitively.

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    // Pin a release tag, or use "main-SNAPSHOT" to track the latest commit.
    compileOnly("com.github.HanielCota.MenuFramework:menu-paper:v0.2.0")
    // Add menu-folia as well when targeting Folia:
    // compileOnly("com.github.HanielCota.MenuFramework:menu-folia:v0.2.0")
}
```

### 2. Bootstrap it in `onEnable`

```java
MenuFramework framework =
    MenuFramework.builder(this)
        .scan("com.example.plugin.menu")
        .build();
```

Tear it down in `onDisable` so listeners, tick tasks and open menus are cleaned up:

```java
@Override
public void onDisable() {
  framework.shutdown();
}
```

### 3. Open a menu

```java
framework.open(player, new MenuId("main"));
```

YAML files live in `plugins/<PluginName>/menus/<id>.yml`.

> 🤖 **Using an AI assistant?** Point it at [`AGENTS.md`](AGENTS.md), a complete and verified API
> reference written for code-generating tools, and [`docs/menu.schema.json`](docs/menu.schema.json)
> for YAML validation and editor autocomplete. [`llms.txt`](llms.txt) indexes both.

---

## 🧩 Features

| | Feature | What it does |
|---|---|---|
| 🧾 | **Static menus** | `@Menu` + `@Button` classes, appearance in YAML. |
| 📚 | **Pagination** | `@Paginated` provider sliced into pages with a mask layout. |
| ⚛️ | **Reactive state** | `@Reactive State<?>` drives coalesced, diff-based re-renders. |
| ⏱️ | **Auto-update** | `@Tick` runs on a schedule for countdowns & animations. |
| 🪝 | **Lifecycle hooks** | `@OnOpen` / `@OnClose` run as the view opens and closes. |
| 🧮 | **Placeholders** | Per-viewer PlaceholderAPI tokens (soft dependency). |
| 💎 | **Rich items** | Amount, glow, unbreakable, model data, tooltip flags. |
| 🔐 | **Permissions & cooldowns** | Gate opens and clicks; rate-limit per player. |
| 🔁 | **Hot-reload** | Reload YAML at runtime (sync, reported, or async). |
| 🧵 | **Paper & Folia** | One API, correct thread on each platform. |

---

## 📖 Guide

<details>
<summary><b>📚 Paginated &amp; reactive menus</b></summary>

<br>

```java
@Menu(id = "catalog")
public final class CatalogMenu {

  @Reactive private final State<Category> category = State.of(Category.TOOLS);

  @Button(id = "next-category")
  public void nextCategory() {
    category.set(category.get().next()); // re-renders the open view
  }

  @Paginated
  public List<MenuItem> products() {
    return products.in(category.get()).stream().map(this::item).toList();
  }
}
```

The pagination **mask** maps characters to slots:

| Symbol | Meaning |
|:---:|---|
| `X` | content slot |
| `#` | border slot |
| `<` | previous page |
| `>` | next page |
| (space) | empty slot |

```yaml
title: "<gold>Catalog</gold>"
rows: 6
pagination:
  mask:
    - "#########"
    - "#XXXXXXX#"
    - "#XXXXXXX#"
    - "#XXXXXXX#"
    - "#XXXXXXX#"
    - "#<#####>#"
  previous-button: { material: ARROW, name: "<yellow>Previous</yellow>" }
  next-button: { material: ARROW, name: "<yellow>Next</yellow>" }
```

</details>

<details>
<summary><b>⏱️ Auto-updating menus (countdowns &amp; animations)</b></summary>

<br>

A `@Tick` method runs on a fixed schedule while the menu is open, on the view's owning thread, so it
may update a `@Reactive State<?>`, which drives the usual coalesced, diff-based re-render. The tick
starts on open and is cancelled on close (no leaked task).

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

</details>

<details>
<summary><b>🧮 Placeholders (PlaceholderAPI)</b></summary>

<br>

Paginated menus resolve `%placeholders%` per viewer in the **page content** and the **title**, as a
PlaceholderAPI soft dependency: installed, the tokens are filled for each player; absent, the text is
left as-is. Resolution runs before MiniMessage parsing and only for the open view, so each player
sees their own values without cross-player cache bleed.

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

Values that change over time refresh on the next re-render. Pair them with `@Tick` for a live value.

> 🛡️ **Security:** placeholder values are MiniMessage-escaped before parsing, so a player whose name
> or nickname contains tags like `<click>` or `<hover>` cannot inject live components into another
> viewer's menu. The author's own template tags are still parsed.

</details>

<details>
<summary><b>🪝 Lifecycle hooks</b></summary>

<br>

`@OnOpen` and `@OnClose` methods on a paginated menu run when the view opens and closes, on the
view's owning thread. Each takes no arguments or a single `Player`. Reactive state and ticks are torn
down for you before `@OnClose` runs.

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

</details>

<details>
<summary><b>💎 Rich items</b></summary>

<br>

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
    model-data: 1001
    flags: [HIDE_ATTRIBUTES, HIDE_UNBREAKABLE]
```

In code (fluent and immutable):

```java
Icon icon = Icons.of(Material.DIAMOND)
    .named("<aqua>Gem</aqua>")
    .amount(16)
    .glowing()
    .hiding(ItemFlag.HIDE_ATTRIBUTES);
```

</details>

<details>
<summary><b>🔐 Permissions &amp; cooldowns</b></summary>

<br>

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

Rate-limit a button per player with `cooldownMillis`; a click made while cooling down is silently
dropped (permission is checked first, so a denied click never starts the cooldown). The window is
per player and survives reopening the menu.

```java
@Button(id = "daily", cooldownMillis = 3000)
public void claimDaily(MenuClick click) {
  click.message("<green>Claimed.</green>");
}
```

</details>

<details>
<summary><b>🧰 Bootstrapping with dependencies</b></summary>

<br>

Use a custom instantiator when menus have constructor dependencies (e.g. a DI container):

```java
MenuFramework.builder(this)
    .instantiator(type -> container.create(type))
    .scan("com.example.plugin.menu")
    .build();
```

</details>

---

## 🔁 Reload

```java
// Synchronous
boolean reloaded = framework.reload(new MenuId("main"));
int count = framework.reloadAll();

// Reported
ReloadReport report = framework.reloadAllReport();

// Async YAML IO
framework.reloadAllReportAsync()
    .thenAccept(r -> sender.sendMessage(Component.text(r.successCount() + " reloaded")));
```

Only YAML IO and parsing run asynchronously. Menu compilation and `ItemStack` creation run on the
plugin scheduler, since Bukkit inventory APIs are not thread-safe. Reloads are cached by file
metadata plus a CRC32 content checksum, so unchanged files are not parsed again.

**Validation** (early, with a clear message rather than a stack trace):

- `rows` must be between `1` and `6`
- button slots must fit the menu size
- pagination masks must match the menu rows, be 9 columns wide, and contain at least one `X`
- material names are validated against Bukkit's `Material` registry

---

## 🛠️ Build

Requires a **JDK 25** toolchain (auto-provisioned via the Foojay resolver on CI/JitPack).

```bash
./gradlew clean test build
```

```powershell
.\gradlew.bat clean test build
```

The example plugin shadow jar is produced at:

```text
example-plugin/build/libs/example-plugin-0.2.0.jar
```

CI runs the full suite and shaded build on **Ubuntu** and **Windows** with Java 25.

---

## 📦 Modules

| Module | Responsibility |
|---|---|
| `menu-core` | Platform-free annotations, config, compiler, state and merge logic. |
| `menu-paper` | Paper-facing facade, listener, rendering and registry. |
| `menu-folia` | Folia scheduler implementation. |
| `example-plugin` | Runnable example plugin using the public API. |

Target stack: **Java 25 · Paper API 26.1.2 (Minecraft 1.21+) · Adventure / MiniMessage · Gradle**.

---

## 📄 License

Released under the [MIT License](LICENSE) © 2026 Haniel Fialho.

<div align="center">
<sub>Built for the Paper &amp; Folia ecosystem. Contributions welcome.</sub>
</div>
