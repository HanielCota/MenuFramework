# MenuFramework — Directives for AI Assistants

> Use this file when generating, reviewing or refactoring code that uses MenuFramework.
> These rules are designed to keep code idiomatic, safe and free of unnecessary abstractions.

## 1. Prefer high-level APIs first

Start with the simplest API that solves the problem. Only drop to low-level APIs when the high-level component cannot express the requirement.

| Use case | Preferred API | Avoid |
|----------|---------------|-------|
| Simple static/informational menu | `MenuBuilder` DSL | Manual `Menu` implementation |
| Confirmation dialog | `Menus.confirmation(...)` | Hand-written `ConfirmationMenu` |
| Settings with boolean toggles | `MenuBuilder.toggle(...)` / `ToggleButton` | Manual `updateState` for each toggle |
| Synchronous pagination | `PaginationComponent` | `Paginator` + `PaginationLayout` boilerplate |
| Asynchronous pagination | `AsyncPaginationComponent` | `AsyncPaginator` + `AsyncPageState` + `PageStateAdapter` |
| Countdown / timer | `CountdownComponent` | Manual periodic task |
| Static list in a region | `ListComponent` | Manual loop over slots |

## 2. Keep menus stateless

Menus must not store viewer-specific mutable state in fields. State belongs to the immutable session object passed through callbacks.

Bad:
```java
public class BadMenu implements Menu<BadMenu.State> {
  private int playerClicks; // WRONG
}
```

Good:
```java
public record State(int playerClicks) {
  public State increment() {
    return new State(this.playerClicks + 1);
  }
}
```

## 3. Never mutate Bukkit inventories directly

Use `MenuCanvas` inside `render`. Do not call `player.getOpenInventory().getTopInventory().setItem(...)` from callbacks.

## 4. Do not block region threads

- `render`, click handlers, `onOpen` and `onClose` run on the entity scheduler.
- Database / HTTP / file work must use `executeAsync`, `AsyncPaginator` or `AsyncPaginationComponent`.
- Do not capture `Player`, `World`, `Inventory` or shared `ItemStack` in async work.

## 5. Do not invent framework features

Only use APIs documented in this repository:

- `com.hanielfialho.menuframework.api`
- `com.hanielfialho.menuframework.api.component`
- `com.hanielfialho.menuframework.api.dsl`
- `com.hanielfialho.menuframework.api.pagination`
- `com.hanielfialho.menuframework.api.pagination.async`
- `com.hanielfialho.menuframework.api.task`
- `com.hanielfialho.menuframework.testing`

Do not create wrapper classes, custom schedulers, reflection-based hooks or "helper" layers around `MenuFramework`, `MenuManager` or `MenuSession` unless the user explicitly asks.

## 6. Use named slots and layouts instead of magic numbers

Prefer named slots. For common patterns use the pre-built layouts:

```java
MenuLayout.standardPage(6); // slots: previous, indicator, next
MenuLayout.confirmation();  // slots: confirm, message, cancel
```

If raw coordinates are clearer for a one-off, that is acceptable.

## 7. Feedback and themes

Use `StandardMenuFeedbackSignals` and theme keys. Do not hard-code sound / particle logic inside click handlers unless the default behavior is genuinely wrong.

## 8. Testing with `MenuTestHarness`

Write tests with the harness for synchronous state transitions. Use the provided assertions:

```java
MenuTestHarness.create(menu, player, State.initial())
    .assertItem("slot", Material.EMERALD)
    .assertClickable("slot")
    .click("slot", ClickType.LEFT)
    .assertState(s -> s.value() == 1, "expected value 1");
```

Do not wrap the harness in heavy abstractions.

## 9. Lifecycle

- Create `MenuFramework` in `JavaPlugin#onEnable()`.
- Call `MenuFramework#shutdown()` in `JavaPlugin#onDisable()`.
- Keep one framework instance per plugin.

## 10. What to avoid

- Generic "MenuManager" wrappers.
- Base classes for menus.
- Annotations or reflection to define menus.
- Inventing new thread models.
- Deep inheritance hierarchies.
- Caching `MenuInteraction` instances.

When in doubt, generate code that looks like the examples in `examples/src/main/java/com/hanielfialho/menuframework/example/menu/`.
