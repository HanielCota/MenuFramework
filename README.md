# MenuFramework

Reusable inventory menu framework for Paper 1.21.1+.

The framework provides a small fluent API for plugin developers and keeps rendering,
session lifecycle, interaction handling, pagination, and cache ownership separated
internally.

## Requirements

- Java 21+
- Paper 1.21.1+
- Gradle Java toolchain 21

## Dependencies

| Dependency | Version | Scope |
| --- | --- | --- |
| paper-api | 1.21.1-R0.1-SNAPSHOT | provided |
| caffeine | 3.1.8 | compile |
| fastutil | 8.5.13 | compile |

## Architecture

```text
MenuFramework
  Public API
    MenuFramework, MenuService, MenuSession, ClickContext, MenuBuilder

  Definitions
    MenuDefinition, SlotDefinition, ItemTemplate, PaginationConfig

  Runtime
    MenuRuntime wires registries, render engine, sessions, dispatchers, and caches

  Cache
    MenuCacheFactory centralizes Caffeine cache construction and tuning

  Session
    MenuSessionImpl delegates to:
      MenuSessionState
      SessionRenderer
      MenuInteractionController
      SessionLifecycle
      RefreshScheduler

  Rendering
    RenderEngine selects StaticRenderStrategy or PaginatedRenderStrategy
    SlotRenderers owns shared slot/fill rendering logic
    PageApplier clones cached page items at the inventory boundary

  Events
    MenuListener forwards Bukkit events to internal MenuEventRouter
```

## Quickstart

```java
public final class MyPlugin extends JavaPlugin {

  private MenuService menus;

  @Override
  public void onEnable() {
    menus = MenuFramework.create(this);

    ItemTemplate fill = ItemTemplate.builder(Material.BLACK_STAINED_GLASS_PANE)
        .name(Component.text(" "))
        .build();

    MenuFramework.builder("main", menus)
        .rows(3)
        .title("<green>Main Menu")
        .fillItem(fill)
        .slot(13, ItemTemplate.builder(Material.DIAMOND)
            .name("<gold>Click")
            .build(),
            clickContext -> clickContext.reply("<yellow>You clicked!"))
        .register();
  }

  public void openMain(Player player) {
    menus.open(player, "main");
  }
}
```

## Pagination

```java
MenuFramework.builder("items", menus)
    .rows(6)
    .title("<aqua>Items")
    .paginate(PaginationConfig.builder()
        .contentSlots(SlotPattern.BORDERED)
        .navigationSlots(List.of(45, 53))
        .previousTemplate("prev_button")
        .nextTemplate("next_button")
        .build())
    .addItem(ItemTemplate.builder(Material.PAPER)
        .name("<yellow>Item")
        .build(),
        clickContext -> clickContext.reply("<green>Selected"))
    .register();
```

## API Notes

- Plugin code should prefer `MenuFramework.create(...)`, `MenuFramework.builder(id, service)`, `MenuService#open(...)`, and `ClickHandler` callbacks.
- Bukkit listener dispatch is internal and is not exposed through `MenuService` or `MenuSession`.
- Refresh/tick feature execution is bridged back onto the Bukkit main thread before touching Bukkit APIs.

## Performance Notes

- Item templates are cached as base `ItemStack`s and cloned before use.
- Paginated page layouts are cached by menu id, requested page, and content hash.
- Cached page `ItemStack`s are defensively copied at the inventory boundary to avoid mutation leaks.
- Session lookup and menu definitions use concurrent/cache-backed registries.
- Caffeine construction is centralized in `internal.cache.MenuCacheFactory`; feature classes own behavior, cache package owns policy.

## License

MIT
