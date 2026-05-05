# MenuFramework — LLM Context Guide

> This document provides high-level architectural context for AI assistants working with the MenuFramework codebase.

---

## Project Overview

MenuFramework is a **Java 21** inventory menu framework targeting **Paper 1.21.1+**. It provides a fluent API for plugin developers to create static and paginated GUIs while internally handling rendering, session lifecycle, interaction dispatch, and caching.

**Build System:** Gradle  
**Key Libraries:** Paper API, Caffeine (caching), FastUtil (primitive collections), Lombok (boilerplate reduction), JSpecify (null-safety annotations)  
**Testing:** JUnit 5 + MockBukkit

---

## Directory Structure

```
src/main/java/com/github/hanielcota/menuframework/
├── api/                        # Public interfaces (plugin-facing)
│   ├── MenuService.java        # Main runtime API (composite of all services)
│   ├── MenuSession.java        # Per-player menu instance
│   ├── ClickContext.java       # Interaction context passed to handlers
│   ├── ClickHandler.java       # Functional interface for slot clicks
│   ├── MenuFeature.java        # Extensible menu behavior (sounds, refresh, etc.)
│   ├── MenuFeatures.java       # Built-in feature implementations
│   └── *Service.java           # Granular service interfaces (definition, template, opening, etc.)
│
├── builder/                    # Fluent API for menu construction
│   ├── MenuBuilder.java        # Main builder (slots, layout, pagination, features)
│   └── MenuRegistrar.java      # Final step that registers with MenuService
│
├── definition/                 # Immutable configuration records
│   ├── MenuDefinition.java     # Complete menu spec (title, size, slots, pagination, features)
│   ├── SlotDefinition.java     # Single slot mapping (template + optional handler)
│   ├── ItemTemplate.java       # ItemStack blueprint (material, name, lore, glow, etc.)
│   ├── PaginationConfig.java   # Page layout configuration
│   └── SlotPattern.java        # Predefined slot patterns (e.g., BORDERED)
│
├── internal/                   # Implementation details (plugins should NOT use directly)
│   ├── DefaultMenuService.java # Concrete MenuService implementation
│   ├── MenuRuntime.java        # Central registry holder (definitions, sessions, rendering)
│   ├── MenuRuntimeFactory.java # Wires all internal components together
│   ├── MenuFrameworkInitializer.java # Bootstraps the framework with a plugin
│   │
│   ├── cache/                  # Caffeine cache construction
│   │   └── MenuCacheFactory.java
│   ├── config/                 # Configuration validation utilities
│   │   └── ConfigValidator.java
│   ├── dispatch/               # Event routing from Bukkit to internal handlers
│   │   ├── MenuEventRouter.java
│   │   ├── DefaultMenuEventRouter.java
│   │   └── ClickDispatcher.java
│   ├── event/                  # Bukkit event listener
│   │   └── MenuListener.java
│   ├── interaction/            # Click processing and policy enforcement
│   │   ├── MenuInteractionController.java
│   │   ├── ClickExecutor.java
│   │   └── InteractionPolicy.java
│   ├── item/                   # ItemStack caching and factory
│   │   ├── ItemStackFactory.java
│   │   └── CachedItemStackFactory.java
│   ├── registry/               # In-memory registries (definitions, templates, sessions, dynamic content)
│   │   ├── MenuRegistry.java
│   │   ├── SessionRegistry.java
│   │   ├── DynamicContentRegistry.java
│   │   └── ItemTemplateRegistry.java
│   ├── render/                 # Rendering engine (static vs paginated)
│   │   ├── RenderEngine.java
│   │   ├── RenderEngineFactory.java
│   │   ├── StaticRenderStrategy.java
│   │   ├── PaginatedRenderStrategy.java
│   │   ├── SlotRenderer.java
│   │   ├── NavigationRenderer.java
│   │   ├── PageApplier.java
│   │   ├── DynamicContentResolver.java
│   │   └── SlowRenderLogger.java
│   ├── session/                # Session lifecycle and state management
│   │   ├── MenuSessionImpl.java      # Main session implementation
│   │   ├── MenuSessionImplFactory.java
│   │   ├── MenuSessionState.java     # Immutable state holder
│   │   ├── SessionRenderer.java
│   │   ├── SessionLifecycle.java     # Dispose, cleanup, feature firing
│   │   ├── RefreshScheduler.java
│   │   ├── SessionFactory.java
│   │   ├── ClickContextImpl.java
│   │   ├── PlayerResolver.java
│   │   └── ActiveSlotRegistry.java
│   ├── server/                 # Bukkit abstraction layer
│   │   ├── ServerAccess.java
│   │   └── BukkitServerAccess.java
│   └── text/                   # Text utilities
│       └── MiniMessageProvider.java
│
├── pagination/                 # Pagination engine and page models
│   ├── PaginationEngine.java
│   ├── PaginationEngineFactory.java
│   ├── PageView.java
│   └── PageCacheKey.java
│
└── scheduler/                  # Thread scheduling abstraction
    ├── SchedulerAdapter.java
    └── PaperSchedulerAdapter.java
```

---

## Key Design Patterns

### 1. **Fluent Builder Pattern**
`MenuBuilder` provides chainable configuration. It collects slot definitions, layout bindings, pagination config, and features, then produces a `MenuRegistrar` which registers the final `MenuDefinition` with `MenuService`.

### 2. **Strategy Pattern**
`RenderStrategy` interface with two implementations:
- `StaticRenderStrategy` — renders fixed slots only
- `PaginatedRenderStrategy` — handles dynamic content + pagination + navigation

The `RenderEngine` selects the appropriate strategy based on whether pagination is configured.

### 3. **Registry Pattern**
All data is stored in typed registries:
- `MenuRegistry` — menu definitions + item templates + dynamic content providers
- `SessionRegistry` — active player sessions (UUID → MenuSession)
- `DynamicContentRegistry` — dynamic content items and providers per menu ID

### 4. **Factory Pattern**
Multiple factories wire dependencies:
- `MenuRuntimeFactory` — constructs the entire internal runtime
- `SessionFactory` — creates sessions asynchronously on the main thread
- `MenuSessionImplFactory` — assembles session components (state, renderer, lifecycle, interactions)
- `RenderEngineFactory` — creates the render engine with appropriate strategies

### 5. **Session State Pattern**
`MenuSessionImpl` delegates to specialized components:
- `MenuSessionState` — holds viewer ID, definition, current page, disposed flag
- `SessionRenderer` — triggers re-rendering
- `MenuInteractionController` — routes clicks to handlers
- `SessionLifecycle` — handles disposal, inventory closing, feature callbacks

---

## Threading Model

**Golden Rule:** All Bukkit API calls happen on the main thread.

- `MenuService#open()` returns a `CompletableFuture<MenuSession>`
- `SessionFactory#create()` schedules session creation via `SchedulerAdapter.runSync()`
- `SessionLifecycle#dispose()` also runs on the main thread
- `RefreshScheduler` uses `runSyncRepeating()` for feature ticks
- The `SchedulerAdapter` abstraction allows testing with synchronous schedulers

**Cache operations** (Caffeine) are thread-safe and can happen off-thread. Only inventory mutation is main-threaded.

---

## Caching Strategy

Three levels of caching:

1. **ItemStack Cache** (`CachedItemStackFactory`)
   - Keys: `ItemTemplate` hash
   - Value: Base `ItemStack` (defensively cloned before inventory placement)

2. **Page Cache** (`PaginationEngine`)
   - Keys: `PageCacheKey` = `(menuId, pageNumber, contentHash)`
   - Value: `PageView` containing computed `ItemStack[]` layout
   - Content hash changes when dynamic content is updated

3. **Session Cache** (`SessionRegistry`)
   - Keys: `UUID` (player)
   - Value: `MenuSessionImpl`
   - Cached for quick lookups during click events

---

## Null-Safety Conventions

- All public API parameters and returns use `@NonNull` by default
- `@Nullable` is explicit for optional values
- Records validate non-null in compact constructors
- `Objects.requireNonNull()` used for defensive checks
- Internal methods may return `@Nullable` when resolution can fail (e.g., `resolvePlayer()`, `resolveSession()`)

---

## Important Gotchas for AI Assistants

### MenuBuilder#build() always uses Component.empty() title
The builder does NOT currently expose a `.title()` method despite `MenuDefinition` supporting titles. To set a title, you must create the `MenuDefinition` manually or extend the builder.

### SessionLifecycle constructor accepts @Nullable MenuSession
`MenuSessionImplFactory` passes `null` initially, then calls `setSession()` after the session object is created. This is intentional to break the circular dependency.

### MenuRuntime has dual-purpose registries
`MenuRegistry` implements multiple interfaces:
- `MenuRegistry` (definitions)
- `ItemTemplateRegistry` (templates)
- `DynamicContentRegistry` (dynamic content)

`MenuRuntime` exposes them through semantic methods (`definitions()`, `templates()`, `dynamicContent()`) but they all delegate to the same `menuRegistry` instance.

### ClickContextImpl is a record
Despite having `@Accessors(fluent = true)` in some versions, `ClickContextImpl` is a Java `record`. Records auto-generate accessors, so Lombok annotations have no effect. Access fields directly: `ctx.player()`, `ctx.session()`.

### Pagination content slots are indices into the dynamic items list, not inventory slots
`contentSlots` in `PaginationConfig` defines which **inventory slots** hold paginated items. The dynamic items list is sliced per page, and each item is placed sequentially into those slots.

### DynamicContentProvider provide() signature
```java
@NonNull List<SlotDefinition> provide(@NonNull Player player, @NonNull MenuSession session)
```
Both parameters are guaranteed non-null when called from `DynamicContentResolver`. The resolver falls back to static content if player or session cannot be resolved.

---

## Testing Conventions

- Unit tests use **MockBukkit** for Bukkit API mocking
- Test plugin class: `MenuTestPlugin`
- Tests cover: builder validation, config validation, pagination logic, session lifecycle, feature callbacks, error handling
- SpotBugs and PMD enforce code quality in CI

---

## Adding New Features

To add a new menu feature:

1. Create a class implementing `MenuFeature`
2. Implement lifecycle hooks: `onOpen()`, `onClose()`, `onTick()`
3. Add a factory method in `MenuFeatures.java`
4. Register the feature via `MenuBuilder#feature()`

Example:
```java
public record CustomFeature(String data) implements MenuFeature {
    @Override
    public void onOpen(MenuSession session) {
        // Logic when menu opens
    }
}
```

---

## Common Tasks

### Create a simple menu
```java
MenuService menus = MenuFramework.create(plugin);
MenuFramework.builder("shop", menus)
    .rows(3)
    .slot(13, ItemTemplate.builder(Material.DIAMOND).name("Buy").build(),
        ctx -> ctx.reply("Clicked!"))
    .register();
```

### Open a menu for a player
```java
menus.open(player, "shop"); // CompletableFuture<MenuSession>
```

### Add dynamic content
```java
menus.setDynamicContentProvider("leaderboard", (player, session) -> {
    return List.of(SlotDefinition.of(-1, template, handler));
});
```

### Close all sessions on plugin disable
```java
menus.shutdown(); // or MenuFramework.shutdown() for singleton
```

---

## Dependencies Graph (Conceptual)

```
Plugin Code
    ↓ (uses)
MenuFramework (entry point)
    ↓ (creates)
MenuService (public API)
    ↓ (delegates to)
MenuRuntime (internal wiring)
    ↓ (holds)
Registries + RenderEngine + SessionFactory + EventRouter
    ↓ (creates)
MenuSessionImpl
    ↓ (composed of)
State + Renderer + InteractionController + Lifecycle
```

---

*Last updated: 2026-05-05*
