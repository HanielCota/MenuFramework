# Prompt 4.5 — In-Game Validation + Naming (results)

## Part A — Naming (DONE)
- Annotation `@State` renamed to `@Reactive`; wrapper stays `State<T>`.
- All FQN that existed only because of the `State`/`State` collision removed
  (`ShopMenu`, `PagedMergerTest` now `import ...state.State` + `@Reactive`).
- Build green on all 3 modules; **25 unit tests** passing; zero collision FQN.

## Environment used
- Real **Paper** server booted headless, **Java 25**, plugin loaded from the shaded jar.
- The offline-extracted server available here is Paper **1.21.8** (MC 1.21.8), not the
  26.1.2 build we compile against. Our plugin targets only long-stable APIs
  (`api-version: '1.21'`), so the 26.1.2-compiled jar loads binary-compatibly on 1.21.8 —
  which is itself a useful compatibility result. A boot on the exact 26.1.2 build needs
  network (paperclip download) and was not available offline.
- Ready-to-run test server prepared at `testserver/` (only this plugin, flat world).

## What was validated on the real server (PASS, with log evidence)

| Item | Result | Evidence (server console) |
|------|--------|---------------------------|
| B6 plugin enables, no stacktrace | **PASS** | `Enabling MenuExample v0.1.0`, `Done (7.4s)`, clean `Disabling` on stop |
| `saveResource` extracts YAML | **PASS** | `plugins/MenuExample/menus/{hello,shop}.yml` created |
| C1 commands registered + routed | **PASS** | `/hello` and `/shop` from console → `Players only!` (guard + routing run) |
| C1 boot error names missing id | **PASS** | broke `shop.yml` → `InvalidMenuException: Button 'change-category' is annotated but missing in YAML; add buttons.change-category` at enable |
| Naming rename safe at runtime | **PASS** | after `@Reactive` rename, paged+reactive+overlay menu still compiles & registers (`Done (7.4s)`, no error) |

## What could NOT be executed here (needs a human-driven session / profiler)

The following require real players clicking inventories, two concurrent players, sustained
load, and a heap profiler with forced GC — none of which can be driven from this
non-interactive environment. **Status: NOT EXECUTED (manual gate pending).**

- C1 (open/click/close in-game; `/hello reload` reflecting an edit live)
- C2 (page nav `<`/`>`, limits, cache hit log on revisit, diff keeps border/nav intact)
- C3 (category change re-renders alone; coalescing = one flush per click; cache invalidation)
- C4 (two players isolated; per-open instance correctness)
- C5 (100× open/close heap baseline; flush-then-close cancels; logout-during-menu)
- C6 (empty/border click cancelled; shift-click/drag cannot move items; outside click)
- C7 (~10–20 players burst; TPS ~20; heap stable; instance-per-open cost under burst)

A MockBukkit harness was attempted to cover the logic of C2–C6 in the JVM, but the only
cached MockBukkit (`mockbukkit-v1.21:4.110.0`) ships unfiltered version metadata
(`${minecraft_version}` placeholders) and rejects every API version at boot — unusable
offline. It was removed so the build stays green.

### Indirect coverage already in unit tests (offline, green)
- Coalescing primitive / change-detection: `StateTest` (`set` notifies only on change;
  `unbind` stops notifications — the anti-leak teardown invariant).
- Page math / limits: `PaginatorTest`, mask validation `MaskLayoutTest`.
- Merge + per-open binding + `@Reactive` discovery + overlay action binding:
  `PagedMergerTest` (binds provider/button to a fresh instance, reads state fields).
- Code paths are wired: `MenuListener.onClose` → `ReactiveView.close()` →
  `ReactiveBinding.close()` → `StateBinding.unbind()` + `Flusher.cancel()`.

## Manual gate — how to run it
1. `testserver/` is prepared (Paper, flat world, this plugin only). Start with:
   `cd testserver && java -jar server.jar --nogui`
2. To see coalescing/cache logs (FINE): the framework logs `coalesced flush` (Flusher)
   and `page cache hit/miss` (PageCache) at `Level.FINE` on the plugin logger. Enable
   FINE for the plugin logger (e.g. via a `log4j2.xml`/JUL config) before observing C2/C3.
3. Join, run `/hello` and `/shop`, and walk items C1–C7 above; record PASS/FAIL.
4. For C5 heap: open/close `/shop` ~100×, force GC (e.g. `spark`/jmap), confirm heap
   returns to baseline (no monotonic growth) and no closed view remains a `State` listener.
5. Note the instance-per-open cost under the C7 burst (each open allocates one menu
   instance + provider/state bind). If it shows up under load, decide mitigation before
   Prompt 05.

## Gate status
- Part A: **DONE** (rename, build green, 25 tests, zero collision FQN).
- Real-server boot + boot-error + command routing: **PASS**.
- Interactive C2–C7: **PENDING manual run** (cannot be automated in this environment).
