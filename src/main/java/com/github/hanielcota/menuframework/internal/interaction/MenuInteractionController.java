package com.github.hanielcota.menuframework.internal.interaction;

import com.github.hanielcota.menuframework.api.ClickHandler;
import com.github.hanielcota.menuframework.internal.session.ActiveSlotRegistry;
import com.github.hanielcota.menuframework.internal.session.MenuSessionState;
import com.github.hanielcota.menuframework.internal.session.PlayerResolver;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public final class MenuInteractionController {

  @NonNull private final MenuSessionState state;
  @NonNull private final ActiveSlotRegistry activeSlots;
  @NonNull private final PlayerResolver playerResolver;
  @NonNull private final ClickExecutor clickExecutor;
  @NonNull private final InteractionPolicy interactionPolicy;

  public boolean handleClick(int rawSlot, @NonNull ClickType clickType) {
    if (state.disposed()) return true;

    Player player = playerResolver.resolveOnline(state.viewerId());
    if (player == null) return true;

    ClickHandler handler = rawSlot >= 0 ? activeSlots.get(rawSlot) : null;
    if (handler != null) {
      clickExecutor.execute(sessionContext(), player, rawSlot, clickType, handler);
      return true;
    }

    var definition = state.definition();
    var view = state.view();
    if (definition == null || view == null) return true;
    return interactionPolicy.shouldCancelUnhandledClick(definition, view, rawSlot, clickType);
  }

  private ClickExecutionContext sessionContext() {
    return new ClickExecutionContext(state.definition());
  }

  public record ClickExecutionContext(
      com.github.hanielcota.menuframework.definition.MenuDefinition definition) {}
}
