package com.github.hanielcota.menuframework.internal.interaction;

import com.github.hanielcota.menuframework.api.MenuHistory;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.api.PlayerInventoryClickHandler;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.internal.session.ActiveSlotRegistry;
import com.github.hanielcota.menuframework.internal.session.ClickContextImpl;
import com.github.hanielcota.menuframework.internal.session.MenuSessionState;
import com.github.hanielcota.menuframework.internal.session.PlayerResolver;
import com.github.hanielcota.menuframework.messaging.MessageService;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jspecify.annotations.NonNull;

public final class MenuInteractionController {

  @NonNull private final MenuSessionState state;
  @NonNull private final ActiveSlotRegistry activeSlots;
  @NonNull private final PlayerResolver playerResolver;
  @NonNull private final ClickExecutor clickExecutor;
  @NonNull private final InteractionPolicy interactionPolicy;
  @NonNull private final MenuService menuService;
  @NonNull private final MenuHistory menuHistory;
  @NonNull private final MessageService messageService;

  public MenuInteractionController(
      @NonNull MenuSessionState state,
      @NonNull ActiveSlotRegistry activeSlots,
      @NonNull PlayerResolver playerResolver,
      @NonNull ClickExecutor clickExecutor,
      @NonNull InteractionPolicy interactionPolicy,
      @NonNull MenuService menuService,
      @NonNull MenuHistory menuHistory,
      @NonNull MessageService messageService) {
    this.state = state;
    this.activeSlots = activeSlots;
    this.playerResolver = playerResolver;
    this.clickExecutor = clickExecutor;
    this.interactionPolicy = interactionPolicy;
    this.menuService = menuService;
    this.menuHistory = menuHistory;
    this.messageService = messageService;
  }

  public synchronized boolean handleClick(int rawSlot, @NonNull ClickType clickType) {
    if (state.disposed()) return true;

    Player player = playerResolver.resolveOnline(state.viewerId());
    if (player == null) return true;

    var definition = state.definition();
    var view = state.view();

    int topSize = view.getTopInventory().getSize();

    // Resolve session once to avoid inconsistency
    var session = menuService.getSession(player.getUniqueId()).orElse(null);

    // Handle clicks in player inventory (bottom inventory)
    if (rawSlot >= topSize) {
      PlayerInventoryClickHandler inventoryHandler = definition.playerInventoryClickHandler();
      if (inventoryHandler != null) {
        if (session != null) {
          inventoryHandler.onClick(player, clickType, rawSlot - topSize, session);
        }
        return definition.blockPlayerInventoryClicks();
      }
      return interactionPolicy.shouldCancelUnhandledClick(definition, view, rawSlot, clickType);
    }

    // Handle clicks in top inventory (menu)
    SlotDefinition slotDef = rawSlot >= 0 ? activeSlots.get(rawSlot) : null;
    if (slotDef != null && slotDef.handler() != null) {
      if (session != null) {
        var clickContext =
            new ClickContextImpl(
                session, player, rawSlot, clickType, menuService, menuHistory, messageService);
        clickExecutor.execute(definition, slotDef, slotDef.handler(), clickContext);
      }
      return true;
    }

    return interactionPolicy.shouldCancelUnhandledClick(definition, view, rawSlot, clickType);
  }
}
