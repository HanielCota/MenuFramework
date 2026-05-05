package com.github.hanielcota.menuframework.internal.interaction;

import com.github.hanielcota.menuframework.api.ClickContext;
import com.github.hanielcota.menuframework.api.ClickHandler;
import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.definition.ToggleState;
import com.github.hanielcota.menuframework.interaction.cooldown.CooldownManager;
import com.github.hanielcota.menuframework.interaction.feature.FeatureInvoker;
import com.github.hanielcota.menuframework.interaction.permission.PermissionChecker;
import com.github.hanielcota.menuframework.interaction.permission.PermissionFallbackRenderer;
import com.github.hanielcota.menuframework.interaction.sound.SoundPlayer;
import com.github.hanielcota.menuframework.interaction.toggle.ToggleManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jspecify.annotations.NonNull;

/**
 * Orchestrates click handling by delegating to specialized services.
 */
public final class ClickExecutor {

  private static final Logger log = Logger.getLogger(ClickExecutor.class.getName());

  private final CooldownManager cooldownManager;
  private final PermissionChecker permissionChecker;
  private final PermissionFallbackRenderer permissionFallbackRenderer;
  private final ToggleManager toggleManager;
  private final SoundPlayer soundPlayer;
  private final FeatureInvoker featureInvoker;

  public ClickExecutor(
      @NonNull CooldownManager cooldownManager,
      @NonNull PermissionChecker permissionChecker,
      @NonNull PermissionFallbackRenderer permissionFallbackRenderer,
      @NonNull ToggleManager toggleManager,
      @NonNull SoundPlayer soundPlayer,
      @NonNull FeatureInvoker featureInvoker) {
    this.cooldownManager = cooldownManager;
    this.permissionChecker = permissionChecker;
    this.permissionFallbackRenderer = permissionFallbackRenderer;
    this.toggleManager = toggleManager;
    this.soundPlayer = soundPlayer;
    this.featureInvoker = featureInvoker;
  }

  public void execute(
      @NonNull MenuDefinition definition,
      @NonNull MenuSession session,
      @NonNull Player player,
      int rawSlot,
      @NonNull ClickType clickType,
      @NonNull ClickHandler handler,
      @NonNull SlotDefinition slotDefinition,
      @NonNull ClickContext clickContext) {
    if (cooldownManager.isOnCooldown(player, slotDefinition)) {
      return;
    }

    if (!permissionChecker.hasPermission(player, slotDefinition)) {
      permissionFallbackRenderer.renderFallback(player, rawSlot, slotDefinition);
      return;
    }

    soundPlayer.playClickSound(player, definition, rawSlot, session.view().getTopInventory().getSize());
    featureInvoker.invokeOnClick(definition, clickContext);

    if (toggleManager.isToggleSlot(slotDefinition)) {
      ToggleState toggleState = slotDefinition.toggleStateKey();
      if (toggleState != null) {
        handleToggle(player, session, rawSlot, clickType, slotDefinition, toggleState, clickContext);
      }
      return;
    }

    try {
      handler.onClick(clickContext);
    } catch (Exception exception) {
      log.log(
          Level.WARNING,
          exception,
          () ->
              "menu.click.handler_error menuId=%s playerUuid=%s slot=%d handlerType=%s"
                  .formatted(
                      definition.id(),
                      player.getUniqueId(),
                      rawSlot,
                      handler.getClass().getSimpleName()));
    }
  }

  private void handleToggle(
      @NonNull Player player,
      @NonNull MenuSession session,
      int rawSlot,
      @NonNull ClickType clickType,
      @NonNull SlotDefinition slotDefinition,
      @NonNull ToggleState toggleState,
      @NonNull ClickContext clickContext) {
    toggleManager.handleToggle(player, session, rawSlot, clickType, slotDefinition, toggleState);

    var toggleHandler = slotDefinition.toggleHandler();
    if (toggleHandler != null) {
      toggleManager.invokeToggleHandlerSafely(
          toggleHandler,
          clickContext,
          toggleState.isEnabled(),
          session.menuId(),
          player.getUniqueId().toString(),
          rawSlot);
    }
  }
}
