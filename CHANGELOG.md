# Changelog

All notable changes to MenuFramework are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
