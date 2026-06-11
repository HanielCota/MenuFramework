# Changelog

All notable changes to MenuFramework are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **Declarative live refresh (`@RefreshOn`).** Annotate a paginated menu with
  `@RefreshOn(SomeEvent.class)` and the framework re-renders the open view whenever that Bukkit event
  fires — re-running the `@Paginated` provider so the menu reflects data it reads but does not own (a
  balance changed elsewhere, an admin action). The subscription is registered while the view is open
  and removed on close, so there is no listener to wire or unregister and nothing to forget. This is
  the declarative counterpart to calling `session(player).refresh()` from a hand-written listener. The
  annotation lives in the Paper layer (`dev.haniel.menu.paper.annotation.RefreshOn`) because it
  references Bukkit event types; event registration is abstracted behind `RefreshSubscriber` (Bukkit
  binding isolated, lifecycle testable without a server). Static menus reject it at boot.
- **Typed open arguments (`@Arg`).** `MenuFramework.open(player, id, argument)` and
  `open(player, type, argument)` open a paginated menu *for* a target, amount or any typed context.
  The argument is injected into every `@Arg` field whose declared type it is assignable to, before the
  first `@Paginated` render (mirroring `@Viewer`), so a menu can be opened with context without a
  hand-rolled session carrier between menus. A `@Arg` field must be a non-final, non-static reference
  type; static menus reject the annotation. Opening with an argument that matches no `@Arg` field is a
  loud `InvalidMenuException`, so a type mismatch fails fast instead of leaving the field silently
  null. The `MenuOpener` navigation interface is unchanged; the argument overloads live on the
  `MenuFramework` facade and `MenuRegistry`.

### Changed

- **`PaperMenu.open` signature.** `open(Player)` is now a default that delegates to the new
  `open(Player, Object argument)`. Internal callers and the no-argument facade methods are unaffected.

## [0.2.0] - 2026-06-09

### Added

- **Menu-authoring DX (Paper).** Six opt-in additions for richer menus, each unit-tested where
  reachable without a server:
  - `@Viewer` injects the viewing `PlayerId` into a paginated menu before the first `@Paginated`
    render, so a provider knows its viewer without a `State<UUID>` workaround.
  - `MenuClick.open(MenuId | Class)` navigates between menus from a button; the opener is owned by
    the framework (`DeferredMenuOpener` bridges the boot-time cycle), so no plugin needs a late
    framework reference. No managed back-stack by design.
  - `MenuClick.prompt(AnvilPrompt.text() | numeric())` opens an anvil text prompt via the modern
    Paper `AnvilView` API (no NMS); invalid numeric input re-prompts.
  - `MenuFramework.session(Player | PlayerId)` returns a transient handle to a player's open menu
    (`menuId`/`refresh`/`close`), read from the live inventory holder — no view registry.
  - `Icons.head(OfflinePlayer | UUID)` / `Icons.headTexture(base64)` produce `PLAYER_HEAD` icons;
    `HeadSkin` lives in `ItemTraits` so the per-page render cache keys on it, and profiles complete
    from cache only (non-blocking).
- **Custom action-error handler.** `MenuFrameworkBuilder.onActionError(handler)` overrides the
  default logging when a button action throws; the listener now logs a failed action with its full
  stack trace, and a throwing handler is itself contained instead of escaping into Bukkit.

### Changed

- **AI-facing references synced.** `AGENTS.md`, `llms.txt`, `docs/menu.schema.json` and `README.md`
  now document the current public API — `MenuFramework` facade methods (`register`, `reloadReport`,
  `close`, `shutdown`), builder methods (`menusDirectory`, `scheduler`, `onActionError`),
  `AnvilPrompt`, `MenuSession`, `MenuErrorHandler`, the `HIDE_ADDITIONAL_TOOLTIP` flag, and an
  `onDisable`/`shutdown` teardown example.
- **menu-core internals.** A clean-code pass removed duplication (shared `ReflectedMembers` and
  `MergeButtons` helpers, grouped boot-reflection caches) and flattened nested lambdas. Behaviour is
  unchanged; reader/merger/template suites stay green.

### Fixed

- **Reactive teardown on disable.** `MenuLifecycle.closeOpenMenuNow` tears down an open reactive
  view explicitly before closing the inventory, and `shutdown()` unregisters the listener right
  after, so on Folia (or whenever the close event is missed) a view's bound state/ticks no longer
  leak. `ReactivePagedView.close()` is idempotent, so a quit after a normal close cannot run teardown
  twice.
- **`@OnClose` on disconnect.** No-arg `@OnClose` handlers run even when the viewer has disconnected
  (quit backstop); `Player`-accepting handlers are skipped individually rather than dropping the
  whole hook.
- **Shutdown races.** `Flusher.mark()` swallows a scheduler that rejects a task while the plugin is
  disabling instead of leaking it into the state write (stays retryable), and the sync-apply executor
  rejects after shutdown so an in-flight async reload cannot build items against a cleared registry.
- **Reload reporting.** A scheduler rejection when delivering a reload report to a player who has
  left is no longer surfaced as an "asynchronous menu reload failed" error; the report is dropped and
  logged at `FINE`.
- **Boot validation.** Invalid `@Menu`/`@Button` ids and reactive-only annotations on a static menu
  are rejected at boot with a clear `InvalidMenuException`, discovery aggregates all bad menus, and
  `@OnOpen`/`@OnClose` accept only an exact `Player` parameter.

[Unreleased]: https://github.com/HanielCota/MenuFramework/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/HanielCota/MenuFramework/releases/tag/v0.2.0

## [0.1.0] - 2026-06-07

### Added

- **Core.** Annotation-driven menus (`@Menu`, `@Button`, `@Paginated`, `@Reactive`) compiled at boot;
  static, paginated and reactive menus with mask-based layouts; diff-based rendering that updates only
  changed slots; a Folia-aware scheduler abstraction (Paper main thread / Folia region thread); YAML
  hot-reload with caching and ClassGraph `@Menu` discovery.

- **Placeholders (PlaceholderAPI).** Paginated menus resolve `%placeholders%` per viewer in page
  content and the title, as a soft dependency (no-op when PlaceholderAPI is absent). Added a
  platform-neutral `PlaceholderResolver` (core) and `PapiPlaceholders`/`ResolvedIconFactory` (Paper);
  resolution is per-view so the per-player page cache stays correct. Static and overlay/navigation
  buttons remain shared (not per-player resolved).
- **Lifecycle hooks.** `@OnOpen`/`@OnClose` methods on a paginated menu run when the view opens and
  closes, on the view's owning thread, taking no args or a single `Player`. Reflection lives in the
  Paper layer (`HookDefinitions`/`MenuHooks`) since a hook may accept the Bukkit `Player`.
- **Click cooldown.** `@Button(cooldownMillis)` rate-limits a button per player; a click made while
  cooling down is silently dropped. Permission is checked before the cooldown, so a denied click does
  not consume it. Permission and cooldown are unified under `ButtonGuards`.
- **Auto-updating menus.** A `@Tick(period)` method on a paginated menu runs on a fixed schedule
  (on the view's owning thread), updating `@Reactive` state to drive countdowns and animations. Backed
  by a new `PlayerScheduler.scheduleRepeating` (Paper `runTaskTimer` / Folia `runAtFixedRate`); the
  repeating task starts on open and is cancelled on close.
- **Rich item appearance.** Icons and YAML buttons now carry `ItemTraits`: stack `amount`, an
  enchantment `glow`, `unbreakable`, custom `modelData`, and tooltip `flags` (a platform-neutral
  `ItemFlag` enum). Fluent on `Icon`/`Icons` (`Icons.of(DIAMOND).glowing().amount(3)`) and
  declarative in YAML.
- **License.** The project is now released under the MIT License.
- **Publishing.** `menu-core`, `menu-paper` and `menu-folia` publish a Maven artifact (with sources
  and Javadoc jars) and are consumable through JitPack.

### Security

- **MiniMessage tag injection.** Placeholder values are now resolved per `%token%` and escaped
  (`PapiPlaceholders`) before MiniMessage parsing, so player-controlled data (names, nicknames,
  third-party placeholders) can no longer inject live tags such as `<click>`/`<hover>` into another
  viewer's menu title or lore. The author's own template tags are still parsed.
- **Click hardening.** The inventory listener runs at `EventPriority.HIGHEST` so its cancel is the
  last word, cancels `InventoryMoveItemEvent` (hoppers/droppers), and contains any exception thrown
  by a button action instead of letting it escape into Bukkit's event pipeline.

### Fixed

- **Cooldown bypass and leak.** A button's cooldown is now a single shared `Cooldown` reused across
  rebinds, so reopening a paginated menu no longer resets the per-player window. Expired entries are
  swept and the accept/record step is atomic, so the map stays bounded and concurrent clicks cannot
  both pass.
- **Offline clicker.** `PlayerArgumentResolver` throws a clear error instead of injecting a `null`
  `Player` when the clicker disconnects between the click and dispatch.
- **Reactive teardown.** View close is idempotent (`@OnClose` runs exactly once), a stale flush no
  longer writes to a closed inventory, and a reactive view left open by a disconnecting player is
  torn down on `PlayerQuitEvent`.
- **Config validation.** Malformed YAML (bad types, missing `rows`, out-of-range values) now surfaces
  as a descriptive `InvalidMenuException` rather than a raw stack trace; `MenuId` is length-capped and
  `Paginator` rejects a non-positive page capacity instead of dividing by zero.
- **Reload diagnostics.** Reload failures are logged with their stack trace and always carry a
  non-null message; `reload(id)` distinguishes an unknown menu from a failed one.

[0.1.0]: https://github.com/HanielCota/MenuFramework/releases/tag/v0.1.0
