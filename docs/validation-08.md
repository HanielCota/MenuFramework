# Prompt 08 — Real Validation + Hardening

Date: 2026-06-05

## Environment

- Java: `25.0.3` Zulu.
- Paper: official PaperMC download, `26.1.2` build `69`, sha256
  `d30fae0c74092b10855f0412ca6b265c60301a013d34bc28a2a41bf5682dd80b`.
- Folia: official PaperMC download, `26.1.2` build `8`, sha256
  `607afd1c3320008e1ffd2eaee6780ace4419d5f8c527b75e79f259be79ebf57b`.
- Plugin jar: `example-plugin/build/libs/example-plugin-0.1.0-SNAPSHOT.jar`.
- Logs:
  - Paper final boot: `runtime/paper-26.1.2/validation-boot-final.log`
  - Folia initial FAIL: `runtime/folia-26.1.2/validation-boot.log`
  - Folia fixed boot: `runtime/folia-26.1.2/validation-boot-after-fix.log`
  - Bot attempt: `runtime/paper-26.1.2/validation-bot-paper.log`

## Corrections Made

1. Inventory drag hardening:
   - `MenuListener` now cancels `InventoryDragEvent` when the top inventory is a menu.
   - Regression test: `MenuListenerTest.cancelsDragWhenTopInventoryIsMenu`.
   - Reason: C6 would otherwise allow drag insertion/extraction without an
     `InventoryClickEvent`.

2. Folia boot marker:
   - Added `folia-supported: true` to `example-plugin/src/main/resources/plugin.yml`.
   - Reason: Folia refused to load the plugin without this marker.

3. Folia retired callback:
   - `FoliaPlayerScheduler` now passes an explicit no-op retired callback instead of `null`.
   - Reason: Prompt 08 requires retired callbacks to be handled.

4. Validation logging:
   - Builder logs selected scheduler at INFO.
   - `Flusher` logs schedule/coalesce/run/cancel at FINE.
   - Existing `PageCache` FINE hit/miss logs remain in place.

## Paper 26.1.2

| Item | Status | Evidence |
| --- | --- | --- |
| Exact Paper boot | PASS | `Loading Paper 26.1.2-69`, `Enabling MenuExample`, `MenuFramework scheduler: PaperMenuScheduler`, `Done (4.846s)`, clean disable in `validation-boot-final.log`. |
| A1 / C2 pagination in-game | NOT EXECUTED | Requires a 26.1.2-compatible real client. Mineflayer attempt failed before login: `unsupported protocol version: 26.1.2`. |
| A2 / C3 reactive in-game | NOT EXECUTED | Same client limitation. FINE diagnostics now exist for cache/coalescing. |
| A3 / C4 multi-player isolation | NOT EXECUTED | Requires two real 26.1.2 clients. |
| A4 / C5 anti-leak heap | NOT EXECUTED | Requires real open/close cycles plus profiler/forced GC. |
| A5 / C6 inventory security | PARTIAL PASS | Code audit found missing drag cancellation; fixed and covered by unit test. Real client drag/shift/hotbar test still not executed. |
| A6 / C7 load sanity | NOT EXECUTED | Requires 10-20 compatible clients/bots. Current bot stack does not support 26.1.2. |

## Folia 26.1.2

| Item | Status | Evidence |
| --- | --- | --- |
| B7 boot | FAIL -> PASS | Initial FAIL: `Could not load plugin 'MenuExample v0.1.0' as it is not marked as supporting Folia!`. Fixed with `folia-supported: true`. Retest: `Initialized 1 plugin`, `Enabling MenuExample`, `MenuFramework scheduler: FoliaMenuScheduler`, `Done (5.366s)`. |
| B8 reactive re-render on EntityScheduler | NOT EXECUTED | Requires real player click in Folia. Boot log contains no `Thread not owning region`, but no re-render was exercised. |
| B9 coalescing in Folia | NOT EXECUTED | Requires real player state changes. FINE diagnostics now exist. |
| B10 anti-leak Folia | NOT EXECUTED | Requires repeated open/close and region/player lifecycle exercise. Retired callback is now explicit no-op. |
| B11 two players in different regions | NOT EXECUTED | Requires two real clients in separate regions. |

Note: the Folia boot harness reached `Done` and selected the right scheduler, but the process did
not exit cleanly from stdin `stop` within the harness timeout and was killed by the harness. The
boot evidence is still valid; graceful Folia shutdown was not claimed as a validated item.

## Bot Automation Attempt

`runtime/bots/validate-paper-bot.js` starts Paper 26.1.2 and attempts an offline Mineflayer bot.
The server booted and disabled cleanly, but Mineflayer failed before connecting:

```text
Error: unsupported protocol version: 26.1.2
```

This blocks automated inventory-click validation in this environment. The GitHub issue for
Mineflayer 26.1.2 support was still open at validation time.

## Instance-Per-Open Cost

Not measured. No compatible bot/client load source was available for 26.1.2, and no real players
were available in this environment. This remains a required manual measurement before release.

## Final Gate Status

Prompt 08 is not fully green.

Completed:
- Exact Paper boot validation.
- Exact Folia boot validation after fixing a real boot FAIL.
- Inventory drag hardening for C6 with automated regression coverage.
- Folia retired callback hardening.
- Diagnostic logs needed for future C2/C3/B9 evidence.
- Full Gradle build green.

Still pending with real clients/profiler:
- A1-A4, A5 in-game retest, A6.
- B8-B11.
- Instance-per-open cost table.
