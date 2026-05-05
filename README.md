<div align="center">

# MenuFramework

**A modern, high-performance inventory menu framework for Paper 1.21.1+**

[![Java](https://img.shields.io/badge/Java-21+-orange)](https://openjdk.org/)
[![Paper](https://img.shields.io/badge/Paper-1.21.1+-blue)](https://papermc.io/)
[![Gradle](https://img.shields.io/badge/Gradle-8.0+-02303a)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Fluent API · Caching · Pagination · Dynamic Content · Thread-Safe

</div>

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Core Concepts](#core-concepts)
  - [Item Templates](#item-templates)
  - [Click Handlers](#click-handlers)
  - [Pagination](#pagination)
  - [Dynamic Content](#dynamic-content)
  - [Menu Features](#menu-features)
- [Architecture](#architecture)
- [Performance](#performance)
- [License](#license)

---

## Overview

MenuFramework is a **reusable, extensible menu framework** designed for Paper/Spigot plugin developers. It abstracts away the complexity of inventory management, providing a clean fluent API while keeping rendering, session lifecycle, interactions, and caching strictly separated internally.

Whether you need a simple static menu or a paginated, dynamically-updating inventory, MenuFramework handles the heavy lifting so you can focus on your game logic.

```java
// Open a menu in 3 lines
MenuService menus = MenuFramework.create(this);
menus.register(menuDefinition);
menus.open(player, "shop");
```

---

## Features

| Feature | Description |
|---------|-------------|
| **Fluent Builder API** | Create menus with an intuitive, chainable API |
| **Built-in Pagination** | Automatic page splitting with configurable navigation slots |
| **Dynamic Content** | Populate menus with runtime data via providers |
| **Smart Caching** | Caffeine-backed caches for items, pages, and sessions |
| **Thread-Safe** | Concurrent registries and safe async-to-sync bridging |
| **Session Management** | Automatic lifecycle tracking per player |
| **Custom Features** | Extensible feature system (sounds, refresh intervals, etc.) |
| **Zero Leak Design** | Defensive ItemStack cloning at all boundaries |
| **Static Analysis** | SpotBugs + PMD validated for production reliability |

---

## Requirements

- **Java** 21 or higher
- **Paper** 1.21.1+ (or compatible forks)
- **Gradle** 8.0+ (or Maven)

---

## Installation

### Gradle

Add the dependency to your `build.gradle`:

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.github.hanielcota:MenuFramework:1.0.0-SNAPSHOT'
}
```

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.hanielcota</groupId>
    <artifactId>MenuFramework</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## Quick Start

### 1. Initialize the Framework

```java
public final class MyPlugin extends JavaPlugin {

    private MenuService menus;

    @Override
    public void onEnable() {
        // Create a standalone service (manage lifecycle yourself)
        menus = MenuFramework.create(this);
        
        // Or use singleton mode (global instance)
        // menus = MenuFramework.initialize(this);
        
        buildMenus();
    }

    @Override
    public void onDisable() {
        menus.shutdown(); // Clean up all sessions
    }
}
```

### 2. Build and Register a Menu

```java
private void buildMenus() {
    // Background filler item
    ItemTemplate fill = ItemTemplate.builder(Material.BLACK_STAINED_GLASS_PANE)
        .name(" ")
        .build();

    // Create and register a simple menu
    MenuFramework.builder("main", menus)
        .rows(3)
        .title("<green><bold>Main Menu")
        .fillItem(fill)
        .slot(11, ItemTemplate.builder(Material.DIAMOND_SWORD)
            .name("<yellow>Weapons")
            .lore("<gray>Click to browse weapons")
            .build(),
            ctx -> {
                ctx.reply("<green>Opening weapons...");
                ctx.open("weapons");
            })
        .slot(15, ItemTemplate.builder(Material.GOLDEN_APPLE)
            .name("<red>Close")
            .build(),
            ClickContext::close)
        .register();
}
```

### 3. Open the Menu

```java
public void openMainMenu(Player player) {
    menus.open(player, "main"); // Returns CompletableFuture<MenuSession>
}
```

---

## Core Concepts

### Item Templates

Templates are reusable blueprints for inventory items. They are cached internally and cloned before use.

```java
ItemTemplate template = ItemTemplate.builder(Material.EMERALD)
    .name("<green><bold>Buy Now")
    .lore(
        "<gray>Price: <gold>100 coins",
        "<gray>Stock: <yellow>42"
    )
    .glow(true)
    .build();
```

### Click Handlers

Handle player interactions with full context:

```java
(slot, template, ctx) -> {
    Player player = ctx.player();
    
    if (!player.hasPermission("shop.buy")) {
        ctx.reply("<red>You don't have permission!");
        return;
    }
    
    ctx.reply("<green>Purchase successful!");
    ctx.close(); // Close the menu
    // ctx.open("confirmation"); // Open another menu
    // ctx.refresh(); // Refresh current menu
}
```

### Pagination

Create paginated menus effortlessly:

```java
MenuFramework.builder("items", menus)
    .rows(6)
    .title("<aqua>Item Browser")
    .paginate(PaginationConfig.builder()
        .contentSlots(SlotPattern.BORDERED) // Slots 10-16, 19-25, 28-34, 37-43
        .navigationSlots(List.of(45, 53))   // Bottom row corners
        .previousTemplate("prev_button")     // Reference registered template
        .nextTemplate("next_button")
        .build())
    .addItem(ItemTemplate.builder(Material.PAPER).name("<yellow>Item 1").build())
    .addItem(ItemTemplate.builder(Material.PAPER).name("<yellow>Item 2").build())
    // ... more items automatically paginated
    .register();
```

### Dynamic Content

Populate menus with runtime data:

```java
menus.setDynamicContentProvider("leaderboard", (player, session) -> {
    // Fetch live data from your database
    List<PlayerStats> topPlayers = database.getTopPlayers(10);
    
    return topPlayers.stream()
        .map((stats, index) -> SlotDefinition.of(
            -1, // Auto-assign slot
            ItemTemplate.builder(Material.PLAYER_HEAD)
                .name("<gold>#" + (index + 1) + " <white>" + stats.name())
                .lore("<gray>Kills: <red>" + stats.kills())
                .build(),
            ctx -> ctx.reply("<yellow>Selected: " + stats.name())
        ))
        .toList();
});
```

### Menu Features

Attach behaviors to menus:

```java
// Auto-refresh content every 5 seconds
MenuFramework.builder("live_stats", menus)
    .rows(3)
    .title("<green>Live Stats")
    .feature(MenuFeatures.refreshInterval(100)) // 100 ticks = 5 seconds
    .feature(MenuFeatures.soundOnOpen(Sound.BLOCK_NOTE_BLOCK_PLING))
    .feature(MenuFeatures.soundOnClick(Sound.UI_BUTTON_CLICK))
    .register();
```

---

## Architecture

```
MenuFramework
├── Public API
│   ├── MenuFramework (entry point / factory)
│   ├── MenuService (register, open, manage sessions)
│   ├── MenuSession (per-player menu instance)
│   ├── ClickContext (interaction context)
│   └── MenuBuilder (fluent menu construction)
│
├── Definitions (immutable configuration)
│   ├── MenuDefinition
│   ├── SlotDefinition
│   ├── ItemTemplate
│   └── PaginationConfig
│
├── Runtime (internal wiring)
│   ├── MenuRuntime (orchestrates registries + engine)
│   ├── RenderEngine (static vs paginated strategies)
│   ├── SessionFactory (creates and initializes sessions)
│   └── MenuEventRouter (forwards Bukkit events)
│
├── Cache Layer (Caffeine)
│   ├── ItemStack cache (base templates)
│   ├── PageView cache (computed layouts)
│   └── Session cache (active lookups)
│
└── Session Lifecycle
    ├── MenuSessionState (page, disposed flag)
    ├── SessionRenderer (re-renders on refresh/page change)
    ├── MenuInteractionController (click routing)
    ├── SessionLifecycle (dispose, cancel tasks)
    └── RefreshScheduler (feature tick bridge)
```

---

## Performance

MenuFramework is designed for **high-performance servers**:

- **Template Caching**: Base `ItemStack` objects cached as singletons, cloned only when placed in inventory
- **Page Caching**: Paginated layouts cached by `(menuId, page, contentHash)` — O(1) lookup for repeated views
- **Concurrent Registries**: All lookups use thread-safe, cache-backed structures
- **Zero Memory Leaks**: 
  - Defensive `ItemStack` cloning at inventory boundaries
  - Automatic session cleanup on player quit / menu close
  - Refresh task cancellation on dispose
- **Smart Bridging**: All async operations safely bridged back to the Bukkit main thread

---

## License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

Built with modern Java for modern Paper servers.

</div>
