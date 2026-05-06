# MenuFramework - LLM Context Guide

This file is the working context for AI assistants editing MenuFramework. Treat it as source-of-truth documentation for architecture, public API shape, gotchas, and repository conventions.

## Project Summary

MenuFramework is a Java 21 inventory menu framework for Paper 1.21.1+. It lets plugin developers define inventory GUIs with a fluent builder, immutable item/menu definitions, paginated dynamic content, session lifecycle management, click routing, caching, preloading, and optional interaction features.

Build and test stack:

- Gradle wrapper
- Paper API 1.21.1
- Caffeine
- FastUtil
- JSpecify annotations
- JUnit 5
- MockBukkit
- Mockito
- Spotless, SpotBugs, PMD

Current verification snapshot:

- `.\gradlew.bat test spotbugsMain` passes.
- Test result: 161 tests, 0 failures, 0 errors, 25 skipped.
- `rg -n "\belse\b" src\main\java src\test\java` returns no matches.
- PMD is configured with `ignoreFailures = true` and still reports style-oriented findings.

## Current User-Facing Capabilities

- Standalone and singleton service lifecycle through `MenuFramework`.
- Fluent menu definition through `MenuBuilder`.
- MiniMessage titles and item names.
- Immutable `ItemTemplate` records with material, name, lore, flags, amount, glow, custom model data, head texture/UUID, leather color, PDC values, and optional click sound.
- Static slots, navigational slots, layout binding, border fill, empty fill, and pattern fill.
- Paginated content via `PaginationConfig`.
- Static dynamic items via `addItem(...)`.
- Runtime dynamic content via `DynamicContentProvider`.
- Reusable navigation templates through `registerTemplate(...)`.
- Per-player `MenuSession` with refresh, close, dispose, page changes, and partial slot updates.
- `ClickContext` with player, audience, click type, slot, session, messaging, navigation, page control, refresh, and close helpers.
- Menu history through `ctx.open(...)`, `ctx.back()`, and `ctx.hasPreviousMenu()`.
- Permission slots with fallback templates.
- Slot cooldowns plus global anti-spam cooldown.
- Toggle slots with state reapplication after refreshes.
- Player inventory click handling.
- Sound features for open and click events.
- Custom `MenuFeature` and `RefreshingMenuFeature` implementations.
- Async menu preloading through `MenuPreloader`.
- Runtime metrics through `MenuMetrics`.

## Important API Caveats

### Custom `MenuFrameworkConfig`

`MenuFramework.create(plugin, builder)` and `initialize(plugin, builder)` exist, but `MenuFramework.Builder` has a private constructor and no public factory method. External plugin code cannot currently instantiate a custom builder cleanly. Document this caveat; do not show `new MenuFramework.Builder()` examples as usable public API.

### `MenuFeatures.refreshInterval(long)`

`MenuFeatures.refreshInterval(long)` currently returns `void` and only validates the tick value. It does not return `RefreshIntervalFeature`. For ticking menus, document a custom `RefreshingMenuFeature` implementation or fix the factory in code before using it in examples.

### Deprecated Builder Slot Pattern

`MenuBuilder.SlotPattern` is deprecated. Prefer:

- `com.github.hanielcota.menuframework.builder.pattern.SlotPatternStrategy` implementations for builder fill patterns.
- `com.github.hanielcota.menuframework.definition.SlotPattern` for pagination content slot lists.

### Public Builder Flow

`MenuBuilder#build()` returns `MenuRegistrar`; users must call `.build().register()`. There is no direct `.register()` method on `MenuBuilder`.

### Dynamic Content Null Handling

Dynamic providers are allowed by Java to return `null` despite `@NonNull`. The framework now sanitizes provider output:

- `null` list -> `List.of()`
- null list entries -> skipped

Static dynamic content registered through `setDynamicContent(...)` still uses `List.copyOf(...)`, so null entries are rejected there.

### Early Return / No Else

Production and test source currently avoid `else`. Keep future changes aligned with early-return style.

## Directory Map

```text
src/main/java/com/github/hanielcota/menuframework/
├── api/
│   ├── ClickContext.java
│   ├── DynamicContentProvider.java
│   ├── DynamicMenuContentService.java
│   ├── MenuDefinitionService.java
│   ├── MenuDiagnostics.java
│   ├── MenuFeature.java
│   ├── MenuFeatures.java
│   ├── MenuHistory.java
│   ├── MenuMetrics.java
│   ├── MenuOpeningService.java
│   ├── MenuPreloader.java
│   ├── MenuService.java
│   ├── MenuSession.java
│   ├── MenuSessionService.java
│   ├── MenuTemplateService.java
│   ├── MessagingContext.java
│   ├── NavigationContext.java
│   ├── PlayerInventoryClickHandler.java
│   ├── RefreshingMenuFeature.java
│   └── ToggleHandler.java
│
├── builder/
│   ├── MenuBuilder.java
│   ├── MenuRegistrar.java
│   └── pattern/
│       ├── BottomRowPattern.java
│       ├── CheckerboardPattern.java
│       ├── CornersPattern.java
│       ├── SlotPatternStrategy.java
│       └── TopRowPattern.java
│
├── core/
│   ├── cache/MenuCacheFactory.java
│   ├── config/ConfigValidator.java
│   ├── profile/BukkitPlayerProfileService.java
│   ├── profile/PlayerProfileService.java
│   ├── server/BukkitServerAccess.java
│   ├── server/ServerAccess.java
│   └── text/MiniMessageProvider.java
│
├── definition/
│   ├── ItemTemplate.java
│   ├── MenuDefinition.java
│   ├── PaginationConfig.java
│   ├── SlotDefinition.java
│   ├── SlotPattern.java
│   ├── ToggleState.java
│   └── ...
│
├── feature/internal/
│   ├── RefreshIntervalFeature.java
│   ├── SoundOnClickFeature.java
│   └── SoundOnOpenFeature.java
│
├── interaction/
│   ├── cooldown/CooldownManager.java
│   ├── feature/FeatureInvoker.java
│   ├── permission/PermissionChecker.java
│   ├── permission/PermissionFallbackRenderer.java
│   ├── sound/SoundPlayer.java
│   └── toggle/ToggleManager.java
│
├── internal/
│   ├── DefaultMenuPreloader.java
│   ├── DefaultMenuService.java
│   ├── MenuFrameworkInitializer.java
│   ├── MenuRuntime.java
│   ├── MenuRuntimeFactory.java
│   ├── dispatch/
│   ├── event/
│   ├── interaction/
│   ├── item/
│   ├── registry/
│   ├── render/
│   └── session/
│
├── messaging/
├── pagination/
└── scheduler/
```

## Public API Details

### `MenuFramework`

Use for service creation and singleton access.

Available public methods:

- `builder(String id)`
- `builder(String id, MenuService service)`
- `service()`
- `create(Plugin plugin)`
- `create(Plugin plugin, MenuFramework.Builder builder)`
- `initialize(Plugin plugin)`
- `initialize(Plugin plugin, MenuFramework.Builder builder)`
- `initializeOrGet(Plugin plugin)`
- `initializeOrGet(Plugin plugin, MenuFramework.Builder builder)`
- `forceReinitialize(Plugin plugin)`
- `forceReinitialize(Plugin plugin, MenuFramework.Builder builder)`
- `shutdown()`

`MenuFramework.Builder` supports `.scheduler(...)` and `.config(...)`, but is not externally constructible right now.

### `MenuService`

Composes:

- `MenuDefinitionService`
- `MenuTemplateService`
- `DynamicMenuContentService`
- `MenuOpeningService`
- `MenuSessionService`
- `MenuDiagnostics`

Additional method:

- `preloader()`

### `MenuSession`

Key methods:

- `viewerId()`
- `menuId()`
- `view()`
- `currentPage()`
- `setPage(int page)`
- `refresh()`
- `close()`
- `dispose()`
- `isSameView(InventoryView other)`
- `updateSlot(int slot, ItemTemplate template)`
- `updateSlots(Map<Integer, ItemTemplate> slots)`

`updateSlot` and `updateSlots` require non-null templates and fail early if null is passed.

### `ClickContext`

Composes `NavigationContext` and `MessagingContext`.

Methods:

- `player()`
- `audience()`
- `clickType()`
- `session()`
- `slot()`
- `rawSlot()`
- `open(String menuId)`
- `back()`
- `hasPreviousMenu()`
- `setPage(int page)`
- `currentPage()`
- `reply(Component message)`
- `reply(String miniMessage)`
- `close()`
- `refresh()`
- `plugin()`

## Builder Methods

Current `MenuBuilder` methods:

- `title(Component)`
- `title(String miniMessage)`
- `rows(int)`
- `layout(String... layout)`
- `bind(char, ItemTemplate)`
- `bind(char, ItemTemplate, ClickHandler)`
- `bindNavigational(char, ItemTemplate)`
- `slot(int, ItemTemplate, ClickHandler)`
- `navigational(int, ItemTemplate, ClickHandler)`
- `slotWithCooldown(int, ItemTemplate, ClickHandler, long cooldownTicks)`
- `slotWithPermission(int, ItemTemplate, ClickHandler, String permission, ItemTemplate fallbackTemplate)`
- `toggleSlot(int, ItemTemplate enabledTemplate, ItemTemplate disabledTemplate, boolean initialState, ToggleHandler)`
- `fillBorder(ItemTemplate)`
- `fillEmpty(ItemTemplate)`
- `fillPattern(SlotPatternStrategy, ItemTemplate)`
- deprecated `fillPattern(MenuBuilder.SlotPattern, ItemTemplate)`
- `pagination(PaginationConfig)`
- `feature(MenuFeature)`
- `allowPlayerInventoryClicks(boolean)`
- `allowShiftClick(boolean)`
- `onPlayerInventoryClick(PlayerInventoryClickHandler)`
- `dynamicContent(DynamicContentProvider)`
- `addItem(ItemTemplate, ClickHandler)`
- `build()`

## Render Flow

Static menu:

```text
MenuSessionImpl.refresh()
  -> SessionRenderer.refresh()
  -> RenderEngine.render(...)
  -> StaticRenderStrategy.render(...)
  -> SlotRenderer.renderStaticSlots(...)
  -> RenderResult
  -> ActiveSlotRegistry.replaceWith(...)
```

Paginated menu:

```text
MenuSessionImpl.refresh()
  -> SessionRenderer.refresh()
  -> RenderEngine.render(...)
  -> PaginatedRenderStrategy.render(...)
  -> DynamicContentResolver.resolve(...)
  -> PaginationEngine.getOrBuildPage(...)
  -> SlotRenderer.buildPage(...)
  -> PageApplier.apply(...)
  -> NavigationRenderer.render(...)
  -> RenderResult
  -> ActiveSlotRegistry.replaceWith(...)
```

## Interaction Flow

```text
Bukkit InventoryClickEvent
  -> MenuListener
  -> DefaultMenuEventRouter
  -> ClickDispatcher
  -> InteractiveMenuSession.handleClick(...)
  -> MenuInteractionController
  -> InteractionPolicy
  -> PermissionChecker / PermissionFallbackRenderer
  -> CooldownManager
  -> ToggleManager
  -> ClickExecutor
  -> user ClickHandler
```

Important behavior:

- Top inventory clicks are matched against `ActiveSlotRegistry`.
- Bottom inventory clicks are routed only when player inventory handling is enabled.
- Unhandled clicks are canceled according to menu policy.
- Shift-click is blocked by default.
- Feature click hooks are invoked from `ClickExecutor`.

## Session Lifecycle

```text
MenuService.open(...)
  -> close existing player session
  -> SessionFactory.create(...)
  -> create inventory on server thread
  -> open inventory
  -> create MenuSessionImpl
  -> initial refresh
  -> fire onOpen features
  -> schedule refresh task if any feature implements RefreshingMenuFeature
  -> register session
```

Disposal:

- Close commands go through `SessionCommands`.
- `SessionLifecycle` cancels refresh task handles.
- `onClose` hooks are invoked.
- `SessionRegistry` removal disposes removed sessions.
- `MenuListener` handles inventory close, player quit, and plugin disable.

## Caching

### Item Stack Cache

Located in `CachedItemStackFactory`.

- Key: `ItemTemplate`
- Value: base `ItemStack`
- Returned stack is always cloned.

### Page Cache

Located in `PaginationEngine`.

- Key: `PageCacheKey(menuId, pageNumber, contentHash)`
- Value: `PageView`
- Invalidated when dynamic content or menu definitions change.

### Session Cache

Located in `SessionRegistry`.

- Key: player UUID
- Value: `MenuSessionImpl`
- Removal listener disposes old sessions.

## Preloader

`DefaultMenuPreloader` resolves menu content asynchronously using `SchedulerAdapter.runAsync(...)`.

Behavior:

- `preload(menuId)` warms static menu content.
- `preload(player, menuId)` resolves provider content with a lightweight mock `MenuSession`.
- Pre-computes up to 5 pages for paginated content.
- Tracks internal `PreloadState`.
- `invalidate(menuId)` and `invalidateAll()` clear preload state and page cache.

The public `MenuPreloader` interface does not expose `PreloadState`; that is internal.

## Null-Safety Rules

- Use `@NonNull` for public parameters and return types unless null is semantically valid.
- Use `@Nullable` explicitly for optional fields such as handlers, fallback templates, dynamic providers, and session resolution.
- Prefer `Objects.requireNonNull(...)` at public or boundary methods.
- Do not let user-provided lists flow into render/pagination without defensive copying or sanitization.
- `PageView` may contain null `ItemStack` entries because empty inventory slots are represented as null in Bukkit.
- `SlotDefinition.template()` and `.handler()` may be null by design.
- `DynamicContentResolver` and `DefaultMenuPreloader` sanitize dynamic provider returns.

## Code Style Rules

- Production and test source currently have no `else` tokens.
- Prefer guard clauses and early return.
- Keep edits tightly scoped.
- Do not introduce Lombok.
- Keep public API examples aligned with real method names.
- Avoid documenting internal classes as plugin-facing API.
- Use JSpecify annotations consistently.
- Use `List.copyOf`, `Map.copyOf`, clones, or immutable records at boundaries.
- Keep comments sparse and useful.

## Known Issues / Follow-Up Candidates

- Expose a public way to construct `MenuFramework.Builder` for custom config/scheduler.
- Change `MenuFeatures.refreshInterval(long)` to return a `MenuFeature`, probably `new RefreshIntervalFeature(ticks)`.
- Decide whether `feature/internal/RefreshIntervalFeature` should be public API or moved/aliased.
- PMD still reports style findings. SpotBugs is the stricter enforced static check right now.
- Several files may appear modified in `git status` due to Windows line-ending/stat noise even when `git diff --name-only` is empty.
- README examples should avoid unpublished dependency coordinates until release publishing is configured.

## Testing Conventions

- MockBukkit is used for plugin/server-facing integration tests.
- Mockito is used for isolated unit tests.
- `MenuTestPlugin` is the plugin test fixture.
- Some tests are skipped by assumptions because MockBukkit does not implement every Paper scheduler operation.
- Prefer focused regression tests close to the changed component.
- Use `.\gradlew.bat test spotbugsMain` before finalizing changes.

## Safe Examples

### Simple Menu

```java
MenuService menus = MenuFramework.create(plugin);

MenuFramework.builder("shop", menus)
    .rows(3)
    .title("<green>Shop")
    .slot(13, ItemTemplate.builder(Material.DIAMOND).name("<aqua>Buy").build(),
        ctx -> ctx.reply("<green>Clicked"))
    .build()
    .register();
```

### Paginated Menu

```java
PaginationConfig pagination = PaginationConfig.builder()
    .contentSlots(SlotPattern.BORDERED.slots(6))
    .navigationSlots(List.of(45, 53))
    .previousTemplate("prev")
    .nextTemplate("next")
    .build();

MenuFramework.builder("items", menus)
    .rows(6)
    .pagination(pagination)
    .addItem(template, ctx -> ctx.reply("<gray>Item"))
    .build()
    .register();
```

### Dynamic Provider

```java
MenuFramework.builder("dynamic", menus)
    .rows(6)
    .pagination(pagination)
    .dynamicContent((player, session) -> service.items(player).stream()
        .map(item -> SlotDefinition.of(-1, item.template(), item.handler()))
        .toList())
    .build()
    .register();
```

### Custom Refresh Feature

```java
public record RefreshEvery(long refreshIntervalTicks) implements RefreshingMenuFeature {
    @Override
    public void onTick(MenuSession session, Player viewer) {
        session.refresh();
    }
}

builder.feature(new RefreshEvery(20));
```

## Changelog Notes

Latest documented state:

- Added `MenuPreloader`.
- Added JSpecify null-safety.
- Hardened dynamic provider null handling.
- Added title/rows builder support.
- Added menu history, cooldown, permission, toggle, player-inventory click integration.
- Added partial slot updates.
- Added metrics.
- Added sound features.
- Removed Lombok.
- Removed `else` usage from source.
- SpotBugs passes with current code.
