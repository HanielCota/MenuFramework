# In-Game Smoke Test

The unit suite cannot reach the boundaries that need a live Paper/Folia server: building
real `ItemStack`s (the item registry), the Bukkit/Folia schedulers, and actually opening an
inventory. This checklist covers those by hand on a real server before each release.

Build the example plugin and drop it on a test server:

```
./gradlew :example-plugin:shadowJar
# copy example-plugin/build/libs/example-plugin-*.jar into <server>/plugins/
```

## 1. Boot & lifecycle
- [ ] Server starts with **no stack trace**; console logs `MenuFramework: N menu(s) registered`
      and the detected scheduler (`PaperPlayerScheduler` or `FoliaPlayerScheduler`).
- [ ] `plugins/MenuExample/menus/main.yml` and `catalog.yml` were extracted on first boot.
- [ ] Editing a YAML and not reloading does **not** change a menu mid-session.

## 2. Static menu (`/menuexample main`)
- [ ] Menu opens with the configured title, rows and items (names/lore render MiniMessage,
      no `§`/`&` artifacts).
- [ ] A player-head icon renders its skin: `Icons.head(onlinePlayer)` shows that player's skin,
      `Icons.headTexture(base64)` shows the custom skin, and an unknown `Icons.head(uuid)` shows the
      default head — all with **no console error and no main-thread hang** (profiles complete from
      cache only).
- [ ] Every button fires its action exactly once per click.
- [ ] Clicking an empty slot does nothing and does **not** drop/steal the item.
- [ ] Shift-click, number-key (hotbar swap) and double-click **cannot** remove items.
- [ ] Dragging across the menu is cancelled.
- [ ] Opening without the required permission shows the deny message and does not open.

## 3. Paginated menu (`/menuexample catalog`)
- [ ] Page fills content slots in mask order; border and nav controls render correctly.
- [ ] **Next** appears only when a next page exists; **Previous** only after page 0.
- [ ] Paging forward/back shows the correct slice; clicking a content item triggers *that*
      item's action on the *current* page (not a stale page).
- [ ] A partial last page shows no ghost items in trailing slots.
- [ ] Overlay buttons render on top and fire their own action.

## 4. Reactivity (the headline feature)
- [ ] A `@Reactive` state change re-renders the open view within a tick (one re-render even
      for several changes in the same tick — coalescing).
- [ ] Only changed slots flicker (diff writer), not the whole inventory.
- [ ] A live-updating menu (counter/timer in lore) runs for several minutes with **no memory
      growth** — watch the heap / `ItemFactory` component cache stays bounded (max 1024).

## 5. Reload (`/menuexample reload`)
- [ ] Editing a YAML then reloading swaps the menu; already-open views keep the old one until
      reopened.
- [ ] A broken YAML reports a clear failure per menu and does **not** crash or wipe the others.
- [ ] Reload runs YAML IO off the main thread (no TPS hit) and applies on the main thread.

## 6. Folia (if testing on Folia)
- [ ] Menus open and re-render on the player's region thread with no "wrong thread" errors.
- [ ] A player logging out while a re-render is pending drops it cleanly (no error).

## 7. Shutdown / reload-plugin
- [ ] `/reload confirm` or stop unregisters the listener and cancels tasks (no leaked tasks,
      no duplicate listeners after a re-enable).
- [ ] No open-inventory or scheduler errors on disable.

## 8. Anvil text input (`AnvilPrompt`)
The anvil event flow (open, force a clickable result, capture the typed text, re-prompt, cancel) is
the part the unit suite cannot reach — only `AnvilPrompt`'s parse/confirm/cancel logic is covered.
Wire a button to `click.prompt(...)` and check:
- [ ] `AnvilPrompt.text()` opens an anvil with the configured title; the result slot is clickable
      even before typing.
- [ ] Typing a value and clicking the result fires `onConfirm` **once** with the typed text.
- [ ] `AnvilPrompt.numeric()` with non-numeric or blank text does **not** confirm and leaves the
      anvil open; a valid integer confirms and closes.
- [ ] The placeholder item **cannot** be taken (every click is cancelled) and is **not** returned to
      the player on close (no free paper/name-tag in the inventory afterwards).
- [ ] Closing the anvil (Esc) without a valid entry fires `onCancel` exactly once.
- [ ] `onConfirm`/`onCancel` calling `click.open(...)` reopens the menu cleanly (no flicker, no
      double open).
- [ ] Disconnecting with an anvil open leaves no pending entry (reconnect and prompt again works).

## 9. Confirmation dialog (`ConfirmPrompt`)
The dialog inventory build (`ItemFactory`) and the open/click/close event flow are server-only; the
unit suite covers only `ConfirmPrompt`'s value-object behaviour. Wire a button to `click.confirm(...)`
and check:
- [ ] `ConfirmPrompt.titled(...)` opens a 3-row chest with confirm (slot 11) and cancel (slot 15)
      buttons; neither item can be taken.
- [ ] Clicking confirm fires `onConfirm` **once** and closes the dialog; cancel fires `onCancel`
      **once** and closes.
- [ ] Closing the dialog (Esc) without choosing fires `onCancel` **once** — and a preceding
      confirm/cancel click does **not** also fire `onCancel` on the close that follows it.
- [ ] `onConfirm`/`onCancel` calling `click.open(...)` reopens the target menu cleanly.

## 10. Sounds (`click.sound`)
- [ ] A `@Button` calling `click.sound("minecraft:ui.button.click")` plays the sound to the clicker.
- [ ] An `@OnOpen`/`@OnClose` hook calling `player.playSound(...)` plays on open/close.

## 11. Animation (`Animation` + `@Tick`)
- [ ] A paginated menu with a `@Tick`-incremented `@Reactive` counter and `Animation.frame(counter)`
      cycles its icon smoothly while open and stops (no leak) on close.

## 12. Per-viewer visibility (`@Visible`, static + paginated)
The overlay/inventory filtering is wired in the per-open build; the unit suite covers `VisibilityRules`
and the static `MenuHolder` skip/gate. Verify on a real server for both menu kinds:
- [ ] A menu with a `@Visible("x")` rule shows button `x` to a player the rule passes and leaves the
      slot **empty** for one it fails — test once on a static menu and once on a paginated menu.
- [ ] The hidden button is **not clickable** (clicking its empty slot does nothing).
- [ ] Two players opening the same menu can see different buttons at once.
- [ ] `/reload` (or the example reload) keeps the rules working on a static menu.

> Anything that fails here is a regression in code paths the unit suite cannot reach
> (`ItemFactory`, `PaperPlayerScheduler`/`FoliaPlayerScheduler`, inventory open/click, anvil and
> confirm events). File a bug with the menu id, YAML and steps.
