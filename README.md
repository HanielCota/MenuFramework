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

## Get Started

MenuFramework is a reusable inventory GUI framework for Paper plugins. Build menus with a fluent API, paginated dynamic content, click routing, sessions, caching, and more.

### 1. Add to your plugin

```groovy
repositories {
    maven { url = 'https://jitpack.io' }
}

dependencies {
    compileOnly 'io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT'
    implementation 'com.github.HanielCota:MenuFramework:v1.0.0'
}
```

> **Full setup with Shadow + Relocation:** see **[USAGE.md](USAGE.md)**

### 2. Create a menu

```java
public class MyPlugin extends JavaPlugin {
    private MenuService menus;

    @Override
    public void onEnable() {
        menus = MenuFramework.create(this);

        MenuFramework.builder("shop", menus)
            .title("<green>Shop")
            .rows(3)
            .slot(13, ItemTemplate.builder(Material.EMERALD)
                .name("<green>Buy").build(),
                ctx -> ctx.reply("<gray>Purchased!"))
            .build()
            .register();
    }
}
```

### 3. Open it

```java
menus.open(player, "shop");
```

---

## Features

- **Fluent Builder** — chain methods to define menus, slots, borders, and patterns
- **Pagination** — automatic page splitting with configurable navigation slots
- **Dynamic Content** — per-player content providers with async preloading
- **Item Templates** — immutable, cached, and clone-safe item blueprints
- **Click Routing** — permission checks, cooldowns, toggles, and player-inventory interaction
- **Session Lifecycle** — automatic cleanup on quit, close, and plugin disable
- **Cache-Backed Rendering** — Caffeine caches for sessions, pages, and item stacks
- **Null-Safety** — JSpecify annotations across public and internal APIs

---

## Documentation

| File | Content |
|------|---------|
| **[USAGE.md](USAGE.md)** | How to integrate MenuFramework into your plugin (Shadow, JitPack, Gradle) |
| **[API.md](API.md)** | Full API reference: builders, templates, pagination, features, sessions, config |

---

## Requirements

- Java 21+
- Paper API 1.21.1+

---

## Development

```powershell
# Run tests and SpotBugs
.\gradlew.bat test spotbugsMain

# Apply code formatting
.\gradlew.bat spotlessApply
```

Verification status:

- 161 tests, 0 failures, 0 errors, 25 skipped
- `spotbugsMain` passes
- No `else` branches in production code

---

## Changelog

See [GitHub Releases](https://github.com/HanielCota/MenuFramework/releases) for the latest changes.

---

## License

No license file is currently present in this repository. Add one before publishing the framework publicly.
