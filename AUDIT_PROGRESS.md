# Audit Progress

Plugin: **MenuFramework** (library: menu-core/menu-paper/menu-folia + example-plugin).
Java 25 · Paper 26.1.2 (API 1.21) · Folia-aware · Gradle. No DB / Redis / Vault / NMS / HTTP /
PlaceholderAPI / proxy — those audit categories are **N/A**.

## Inventory (verified)
- Main: `ExamplePlugin` (JavaPlugin). Framework facade: `MenuFramework` + `MenuFrameworkBuilder`.
- Lifecycle: `onEnable` wires services + `MenuFramework.builder().scan().build()`; `onDisable` →
  `framework.shutdown()` → `MenuLifecycle.shutdown()`.
- Listeners: `MenuListener` (click/drag/close). Commands: `menuexample` → `MenuCommandService`.
- Schedulers: `MenuScheduler` (forPlayer) → `PaperMenuScheduler`/`FoliaMenuScheduler`; global/sync via
  `MenuLifecycle` (was hardcoded to `Bukkit.getScheduler()` — see F1). Async IO via single-thread pool.
- Persistence: YAML only (`MenuLoader`, Configurate). Caches: `PageCache` (Caffeine, per-view),
  `ItemFactory.components` (bounded), `MenuLoader` (ConcurrentHashMap, content-stamped).
- State: per-view; `MenuCatalog` (ConcurrentHashMap) is the only shared registry.

## Done this session (prior rounds)
15 bugs fixed; 517 tests; JaCoCo 84.4% instr / 86.8% branch. Concurrency, security (path traversal),
cache, lifecycle, render assembly, click-cancellation all audited green. See prior final reports.

## This audit (Folia / inventory / lifecycle lens)
- [x] MenuListener — cancel-everything-while-open via top `getInventory()` + `getRawSlot()`. Closes
      shift-click / number-key / double-click / drag / bottom-inv theft. **Secure.**
- [x] StaticPaperMenu — fresh `MenuHolder`+inventory per open; no shared-inventory dupe. **Safe.**
- [x] plugin.yml — api-version 1.21; perms default true(open)/op(reload); `folia-supported: true`.
- [F1] **FIXED — Folia reload incompatibility (HIGH, probable, not server-reproduced).**
      `MenuLifecycle` sync executor + shutdown used legacy `Bukkit.getScheduler().runTask/cancelTasks`,
      unsupported on Folia → `reloadAllReportAsync` broke `/reload` on Folia. Added
      `MenuScheduler.global()` (Paper main / Folia `GlobalRegionScheduler`); `MenuLifecycle` now uses
      it and guards `cancelTasks` against the Folia `UnsupportedOperationException`. Paper behaviour
      unchanged. Regression test: `shutdownSurvivesFoliaUnsupportedScheduler`. Folia path not
      server-verified (no Folia in CI).
- [F2] **FIXED — Swallowed async exception (MEDIUM, confirmed).** `MenuReloader.reloadAll` had no
      error handler on the reload future. Added `.exceptionally(logFailure)`; constructor now takes a
      `Logger`. Regression test: `logsAsyncReloadFailureInsteadOfSwallowingIt`.

## Result
519 tests green; `clean test build` (+ shadowJar) green; JaCoCo 84.4% instr / 86.8% branch.

## Pass 3 — public API surface & MiniMessage trust boundary
- [x] `MenuClick` (player/playerId/clickType/message/close), `Icons.of(Material)`, `Icon`,
      `PaperClickContext`, `MenuClickArgumentResolver`, `PaperContexts` — all read fully.
- `PaperClickContext` holds a `Player` but is per-click and never stored → no Player leak. ✓
- `Icon` validates material, null-coalesces name/lore, `List.copyOf` lore; record equals/hashCode ok.
- **No new confirmed bug.**
- [S1] **MiniMessage trust boundary (Low / awareness).** `MenuClick.message()` and `Icon` name/lore
      are parsed as *trusted* MiniMessage (all tags). No player-controlled text reaches them in the
      shipped code (config + hardcoded products), so **not exploitable as shipped**. Added a security
      Javadoc note to both (doc-only, no behaviour change) warning devs to escape untrusted input.

## Commands
`./gradlew test` · `./gradlew clean test build` · `./gradlew jacocoTestReport`
