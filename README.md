<div align="center">

# MenuFramework

**Inventory menu framework for Paper 1.21.1+ built with Java 21**

[![Java](https://img.shields.io/badge/Java-21+-orange)](https://openjdk.org/)
[![Paper](https://img.shields.io/badge/Paper-1.21.1+-blue)](https://papermc.io/)
[![Gradle](https://img.shields.io/badge/Gradle-9+-02303a)](https://gradle.org/)
[![CI](https://github.com/HanielCota/MenuFramework/actions/workflows/ci.yml/badge.svg)](https://github.com/HanielCota/MenuFramework/actions/workflows/ci.yml)
[![JitPack](https://jitpack.io/v/HanielCota/MenuFramework.svg)](https://jitpack.io/#HanielCota/MenuFramework)
[![GitHub Release](https://img.shields.io/github/v/release/HanielCota/MenuFramework)](https://github.com/HanielCota/MenuFramework/releases)

Fluent API · Pagination · Dynamic Content · Caching · Sessions · Null-Safety

</div>

---

## Table of Contents

- [Overview](#overview)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Public API Surface](#public-api-surface)
- [Builder Reference](#builder-reference)
- [Item Templates](#item-templates)
- [Click Context](#click-context)
- [Pagination](#pagination)
- [Dynamic Content](#dynamic-content)
- [Permissions, Cooldowns, Toggles, and Player Inventory](#permissions-cooldowns-toggles-and-player-inventory)
- [Menu Features](#menu-features)
- [Preloading](#preloading)
- [Sessions and Metrics](#sessions-and-metrics)
- [Configuration](#configuration)
- [Architecture](#architecture)
- [Performance and Safety](#performance-and-safety)
- [Development](#development)
- [Changelog](#changelog)
- [License](#license)

---

## Overview

MenuFramework is a reusable inventory GUI framework for Paper plugins. It provides a fluent builder for menus, item templates, paginated dynamic content, click routing, player session lifecycle, cache-backed rendering, and optional interaction helpers such as permissions, cooldowns, toggle slots, menu history, sounds, and preloading.

The project targets a modern Java style:

- Java 21 records, pattern matching, `CompletableFuture`, and immutable definitions.
- JSpecify annotations for explicit null-safety.
- Early-return control flow in production code; no `else` branches in `src/main/java`.
- Defensive boundaries for user-provided dynamic content.
- SpotBugs and JUnit coverage for regressions.

---

## Requirements

- Java 21+
- Paper API 1.21.1+
- Gradle wrapper included in the repository

---

## Installation

MenuFramework is published via **[JitPack](https://jitpack.io/#HanielCota/MenuFramework)** and **[GitHub Releases](https://github.com/HanielCota/MenuFramework/releases)**.

### Gradle (with Shadow)

Add JitPack and the dependency to your plugin's `build.gradle`:

```groovy
repositories {
    mavenCentral()
    maven { url = 'https://repo.papermc.io/repository/maven-public/' }
    maven { url = 'https://jitpack.io' }
}

dependencies {
    compileOnly 'io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT'
    implementation 'com.github.HanielCota:MenuFramework:v1.0.0'
}
```

**Important:** Always shade and relocate MenuFramework to avoid conflicts with other plugins:

> **Note:** MenuFramework is published as a **fat-jar** — it already includes Caffeine and FastUtil. You only need the single dependency above.

**Important:** Always shade and relocate MenuFramework to avoid conflicts with other plugins:

```groovy
shadowJar {
    relocate 'com.github.hanielcota.menuframework', 'yourplugin.libs.menuframework'
    archiveClassifier.set('')
}

tasks.build.dependsOn tasks.shadowJar
```

> Replace `yourplugin` with your plugin's package (e.g., `me.haniel.myplugin`).
>
> You no longer need to relocate `caffeine` or `fastutil` separately — they are already bundled inside MenuFramework.

For a complete step-by-step guide, see **[USAGE.md](USAGE.md)**.

---

## Quick Start

### 1. Create a Service

```java
public final class MyPlugin extends JavaPlugin {
    private MenuService menus;

    @Override
    public void onEnable() {
        menus = MenuFramework.create(this);
        registerMenus();
    }

    @Override
    public void onDisable() {
        menus.shutdown();
    }
}
```

You can also use singleton mode:

```java
MenuService menus = MenuFramework.initialize(plugin);
MenuFramework.builder("main"); // uses the singleton service
MenuFramework.shutdown();
```

### 2. Build and Register a Menu

```java
private void registerMenus() {
    ItemTemplate filler = ItemTemplate.builder(Material.BLACK_STAINED_GLASS_PANE)
        .name(" ")
        .build();

    ItemTemplate shop = ItemTemplate.builder(Material.EMERALD)
        .name("<green>Shop")
        .lore(List.of(Component.text("Open the shop")))
        .glow(true)
        .build();

    MenuFramework.builder("main", menus)
        .rows(3)
        .title("<green>Main Menu")
        .fillEmpty(filler)
        .slot(13, shop, ctx -> {
            ctx.reply("<gray>Opening shop...");
            ctx.open("shop");
        })
        .build()
        .register();
}
```

### 3. Open a Menu

```java
menus.open(player, "main")
    .exceptionally(error -> {
        player.sendMessage(Component.text("Could not open menu."));
        return null;
    });
```

`open(...)` returns `CompletableFuture<MenuSession>` because session creation is bridged onto the server thread.

---

## Public API Surface

| Type | Purpose |
| --- | --- |
| `MenuFramework` | Entry point for standalone or singleton service creation. |
| `MenuService` | Main runtime API composed from definition, template, dynamic content, opening, session, diagnostics, and preloader services. |
| `MenuBuilder` | Fluent menu definition builder. |
| `MenuRegistrar` | Result of `build()`, used to register the definition and dynamic content. |
| `MenuSession` | Active player menu session; exposes page, refresh, close, dispose, and slot update operations. |
| `ClickContext` | Callback context for slot clicks, including navigation and messaging helpers. |
| `ItemTemplate` | Immutable item blueprint used by the cached `ItemStack` factory. |
| `SlotDefinition` | Slot item, handler, permission, cooldown, navigation, and toggle metadata. |
| `PaginationConfig` | Content and navigation slot configuration for paginated menus. |
| `MenuPreloader` | Async pre-computation API for menu content and cached pages. |

---

## Builder Reference

```java
MenuFramework.builder("menu-id", menus)
    .title("<gold>Menu Title")
    .rows(6)
    .layout(
        "XXXXXXXXX",
        "X       X",
        "XXXXXXXXX")
    .bind('X', filler)
    .slot(13, item, ctx -> ctx.reply("<green>Clicked"))
    .navigational(45, backButton, ctx -> ctx.back())
    .slotWithCooldown(20, button, handler, 20)
    .slotWithPermission(22, adminButton, handler, "plugin.admin", deniedButton)
    .toggleSlot(24, enabledItem, disabledItem, true, (ctx, enabled) -> {
        ctx.reply(enabled ? "<green>Enabled" : "<red>Disabled");
    })
    .allowPlayerInventoryClicks(true)
    .allowShiftClick(false)
    .onPlayerInventoryClick((player, clickType, slot, session) -> {
        player.sendMessage(Component.text("Clicked player inventory slot " + slot));
    })
    .fillBorder(filler)
    .fillEmpty(filler)
    .feature(MenuFeatures.soundOnOpen(sound))
    .dynamicContent((player, session) -> List.of())
    .build()
    .register();
```

Important builder notes:

- `build()` is single-use. Reuse requires a new `MenuBuilder`.
- `slot(...)` accepts a nullable template and ignores null templates by design.
- `navigational(...)` registers slots that participate in active click handling even when used as pagination/navigation controls.
- `fillEmpty(...)` stores a menu-wide filler rendered into empty inventory slots.
- `fillBorder(...)` and `fillPattern(...)` write static slots only where no slot is already configured.
- `MenuBuilder.SlotPattern` is deprecated; prefer `builder.pattern.*` strategy classes or `definition.SlotPattern` for pagination content slots.

---

## Item Templates

`ItemTemplate` is the only item type accepted by the framework. It is immutable, cacheable, and cloned defensively when converted to an `ItemStack`.

```java
ItemTemplate button = ItemTemplate.builder(Material.PLAYER_HEAD)
    .name("<yellow>Profile")
    .lore(List.of(Component.text("Click to inspect")))
    .amount(1)
    .glow(true)
    .customModelData(1001)
    .head(player.getUniqueId())
    .clickSound(org.bukkit.Sound.UI_BUTTON_CLICK)
    .build();
```

Supported template fields:

- Material, display name, lore, item flags, amount, glow, custom model data.
- Player heads by UUID or base64 texture.
- Leather armor color.
- Persistent data container values for common primitive types.
- Optional Bukkit click sound stored on the template.

---

## Click Context

Handlers receive `ClickContext`, which combines player, slot, click type, navigation, messaging, and session access.

```java
ctx -> {
    Player player = ctx.player();
    ClickType click = ctx.clickType();

    if (click.isRightClick()) {
        ctx.reply("<gray>Right click on slot " + ctx.slot());
        return;
    }

    ctx.setPage(ctx.currentPage() + 1);
}
```

Useful methods:

- `player()`, `audience()`, `clickType()`, `slot()`, `rawSlot()`, `session()`
- `reply(Component)`, `reply(String)`, `plugin()`
- `open(menuId)`, `back()`, `hasPreviousMenu()`
- `setPage(page)`, `currentPage()`
- `refresh()`, `close()`

---

## Pagination

Pagination projects a list of dynamic `SlotDefinition` items into configured inventory slots.

```java
menus.registerTemplate("prev", ItemTemplate.builder(Material.ARROW)
    .name("<yellow>Previous")
    .build());

menus.registerTemplate("next", ItemTemplate.builder(Material.ARROW)
    .name("<yellow>Next")
    .build());

PaginationConfig pagination = PaginationConfig.builder()
    .contentSlots(SlotPattern.BORDERED.slots(6))
    .navigationSlots(List.of(45, 53))
    .previousTemplate("prev")
    .nextTemplate("next")
    .build();

MenuFramework.builder("items", menus)
    .rows(6)
    .title("<aqua>Items")
    .pagination(pagination)
    .addItem(ItemTemplate.builder(Material.DIAMOND).name("<blue>Diamond").build(),
        ctx -> ctx.reply("<blue>Diamond"))
    .addItem(ItemTemplate.builder(Material.EMERALD).name("<green>Emerald").build(),
        ctx -> ctx.reply("<green>Emerald"))
    .build()
    .register();
```

Built-in pagination slot patterns:

- `SlotPattern.FULL`
- `SlotPattern.BORDERED`
- `SlotPattern.CHEST_6`

Navigation buttons use registered template IDs. If the previous or next template is missing, that navigation button is skipped.

---

## Dynamic Content

Dynamic content can be registered statically with `addItem(...)` or supplied at render time with `DynamicContentProvider`.

```java
MenuFramework.builder("leaderboard", menus)
    .rows(6)
    .pagination(PaginationConfig.builder()
        .contentSlots(SlotPattern.BORDERED.slots(6))
        .navigationSlots(List.of(45, 53))
        .build())
    .dynamicContent((player, session) -> leaderboardService.topPlayers().stream()
        .map(entry -> SlotDefinition.of(
            -1,
            ItemTemplate.builder(Material.PLAYER_HEAD)
                .name("<gold>" + entry.name())
                .lore(List.of(Component.text("Score: " + entry.score())))
                .build(),
            ctx -> ctx.reply("<yellow>" + entry.name())))
        .toList())
    .build()
    .register();
```

Null-safety behavior:

- A provider returning `null` is treated as an empty list.
- Null entries returned by a provider are ignored.
- Static dynamic content registered through `setDynamicContent(...)` is defensively copied.
- Updating dynamic content invalidates cached pages for that menu.

---

## Permissions, Cooldowns, Toggles, and Player Inventory

### Permission Slots

```java
builder.slotWithPermission(
    13,
    adminItem,
    ctx -> ctx.reply("<green>Admin action"),
    "myplugin.admin",
    deniedItem);
```

When the viewer lacks permission, the fallback template is rendered and the protected handler is not executed.

### Slot Cooldowns

```java
builder.slotWithCooldown(20, expensiveActionItem, ctx -> {
    ctx.reply("<green>Action executed");
}, 40); // 40 ticks
```

There is also a small global anti-spam cooldown in the interaction layer.

### Toggle Slots

```java
builder.toggleSlot(
    22,
    ItemTemplate.builder(Material.LIME_WOOL).name("<green>Enabled").build(),
    ItemTemplate.builder(Material.RED_WOOL).name("<red>Disabled").build(),
    false,
    (ctx, enabled) -> ctx.reply(enabled ? "<green>On" : "<red>Off"));
```

Toggle state is kept in `MenuSessionState` and re-applied after refreshes.

### Player Inventory Clicks

```java
builder
    .allowPlayerInventoryClicks(true)
    .allowShiftClick(false)
    .onPlayerInventoryClick((player, clickType, slot, session) -> {
        player.sendMessage(Component.text("Bottom inventory slot: " + slot));
    });
```

By default, player inventory clicks and shift-clicks are blocked while a menu session is active.

---

## Menu Features

`MenuFeature` exposes lifecycle hooks:

- `onOpen(MenuSession)`
- `onClose(MenuSession)`
- `onClick(ClickContext)`
- `onTick(MenuSession, Player)`

Built-in factories currently available:

```java
builder.feature(MenuFeatures.soundOnOpen(adventureSound));
builder.feature(MenuFeatures.soundOnClick(adventureSound));
```

For ticking menus, implement `RefreshingMenuFeature`:

```java
public record ClockFeature(long refreshIntervalTicks) implements RefreshingMenuFeature {
    @Override
    public void onTick(MenuSession session, Player viewer) {
        session.refresh();
    }
}

builder.feature(new ClockFeature(20));
```

Note: `MenuFeatures.refreshInterval(long)` currently validates the tick value but does not return a feature. Use a custom `RefreshingMenuFeature` until that factory is adjusted.

---

## Preloading

`MenuPreloader` warms menu content asynchronously and pre-computes up to the first few paginated pages.

```java
menus.preloader().preload("shop");
menus.preloader().preload(player, "leaderboard");
menus.preloader().preloadAll("shop", "leaderboard", "settings");

menus.preloader().invalidate("shop");
menus.preloader().invalidateAll();
```

Use player-specific preload when a dynamic provider depends on `Player` or `MenuSession`.

---

## Sessions and Metrics

```java
menus.getSession(player.getUniqueId()).ifPresent(MenuSession::refresh);

MenuMetrics metrics = menus.getMetrics();
long active = metrics.activeSessions();
long menusRegistered = metrics.registeredMenus();
long cachedPages = metrics.cachedPages();
```

Session API:

- `viewerId()`, `menuId()`, `view()`
- `currentPage()`, `setPage(int)`
- `refresh()`, `close()`, `dispose()`
- `updateSlot(int, ItemTemplate)`
- `updateSlots(Map<Integer, ItemTemplate>)`

`updateSlot` and `updateSlots` require non-null templates and fail fast with `NullPointerException` if a caller passes null.

---

## Configuration

```java
MenuFrameworkConfig config = new MenuFrameworkConfig()
    .sessionCacheMaxSize(1_000)
    .sessionCacheExpireMinutes(10)
    .pageCacheMaxSize(10_000)
    .pageCacheExpireMinutes(15)
    .itemStackCacheMaxSize(4_000)
    .itemStackCacheExpireMinutes(30)
    .logSlowRenders(true)
    .slowRenderThresholdMillis(50);
```

Current API caveat: `MenuFramework.create(plugin, builder)` and `initialize(plugin, builder)` exist, but the nested `MenuFramework.Builder` constructor is private and there is no public factory for it. External plugin code can use default configuration today; custom configuration requires exposing a public builder factory or constructor.

---

## Architecture

```text
Plugin
  -> MenuFramework
  -> MenuService
  -> MenuRuntime
     -> MenuRegistry
     -> SessionRegistry
     -> RenderEngine
        -> StaticRenderStrategy
        -> PaginatedRenderStrategy
     -> PaginationEngine
     -> SessionFactory
     -> MenuEventRouter
     -> MenuPreloader
```

Core implementation areas:

- `api/`: public contracts for plugin developers.
- `builder/`: fluent menu construction.
- `definition/`: immutable records for menus, slots, templates, pagination, toggles.
- `internal/render/`: render strategies, page application, navigation rendering, dynamic content resolution.
- `internal/session/`: session state, renderer, click context, lifecycle, active slot registry, history.
- `internal/interaction/` and `interaction/`: policies, click execution, permissions, cooldowns, sound, toggles.
- `internal/registry/`: definitions, templates, dynamic content, sessions.
- `scheduler/`: Paper scheduler abstraction.
- `core/`: cache, config, server, profile, and text utilities.

---

## Performance and Safety

- `ItemTemplate` objects are converted to cached base `ItemStack`s, then cloned before use.
- `PageView` clones item arrays at boundaries.
- Page cache keys include `(menuId, pageNumber, contentHash)`.
- Dynamic content updates invalidate page cache for that menu.
- Sessions are closed on inventory close, player quit, plugin disable, and service shutdown.
- Refresh tasks are canceled when sessions are disposed.
- Bukkit inventory mutation is bridged to the server thread through `SchedulerAdapter`.
- User-provided dynamic content is sanitized before pagination/rendering.

---

## Development

Run the main verification:

```powershell
.\gradlew.bat test spotbugsMain
```

Optional formatting:

```powershell
.\gradlew.bat spotlessApply
```

Current local verification status:

- 161 tests, 0 failures, 0 errors, 25 skipped.
- `spotbugsMain` passes.
- `rg -n "\belse\b" src\main\java src\test\java` returns no matches.

PMD is configured but currently has style-oriented findings and `ignoreFailures = true`.

---

## Changelog

### Current Snapshot

- Added JSpecify null-safety annotations across public and internal APIs.
- Added menu preloader service for async warm-up and page pre-computation.
- Hardened dynamic content providers against null returns and null entries.
- Added defensive null checks for partial slot updates.
- Removed `else` branches from production code in favor of early-return style.
- Added title and row configuration to `MenuBuilder`.
- Added slot permissions, cooldowns, toggle slots, player-inventory click integration, menu history, metrics, and preloading.
- Added Caffeine-backed caches for sessions, pages, and item stacks.
- Added Paper scheduler abstraction.
- Removed Lombok usage and kept the implementation in Java 21.

See [GitHub Releases](https://github.com/HanielCota/MenuFramework/releases) for versioned releases.

---

## License

[MIT](LICENSE)
