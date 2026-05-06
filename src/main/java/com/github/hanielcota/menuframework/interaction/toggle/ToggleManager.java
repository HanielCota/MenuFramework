package com.github.hanielcota.menuframework.interaction.toggle;

import com.github.hanielcota.menuframework.api.ClickContext;
import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.api.ToggleHandler;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.definition.ToggleState;
import com.github.hanielcota.menuframework.internal.session.MenuSessionImpl;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jspecify.annotations.NonNull;

/** Manages toggle slot state transitions and visual updates. */
public final class ToggleManager {

  private static final Logger log = Logger.getLogger(ToggleManager.class.getName());

  /** Returns true if the slot definition represents a toggle slot with valid state. */
  public boolean isToggleSlot(@NonNull SlotDefinition slotDefinition) {
    return slotDefinition.toggle() && slotDefinition.toggleStateKey() != null;
  }

  /** Handles a toggle click: flips state, updates visual, and invokes the toggle handler. */
  public synchronized void handleToggle(
      @NonNull MenuSession session, int rawSlot, @NonNull ToggleState toggleState) {
    toggleState.setEnabled(!toggleState.isEnabled());
    session.updateSlot(rawSlot, toggleState.currentTemplate());
    if (session instanceof MenuSessionImpl impl) {
      impl.state().toggleStates().put(rawSlot, toggleState);
    }
  }

  /** Invokes the toggle handler safely, catching and logging exceptions. */
  public void invokeToggleHandlerSafely(
      @NonNull ToggleHandler toggleHandler,
      @NonNull ClickContext context,
      boolean enabled,
      @NonNull String menuId,
      @NonNull String playerUuid,
      int rawSlot) {
    try {
      toggleHandler.onToggle(context, enabled);
    } catch (Exception exception) {
      log.log(
          Level.WARNING,
          exception,
          () ->
              "menu.click.toggle_error menuId=%s playerUuid=%s slot=%d"
                  .formatted(menuId, playerUuid, rawSlot));
    }
  }
}
