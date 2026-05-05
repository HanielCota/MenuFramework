package com.github.hanielcota.menuframework.internal.interaction;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.hanielcota.menuframework.api.ClickHandler;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.internal.session.ClickContextImpl;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jspecify.annotations.NonNull;

@Slf4j
@RequiredArgsConstructor
public final class ClickExecutor {

  private static final long DEFAULT_COOLDOWN_MS = 100;
  private static final long COOLDOWN_CACHE_SECONDS = 1;
  private static final float DEFAULT_SOUND_VOLUME = 1.0f;
  private static final float DEFAULT_SOUND_PITCH = 1.0f;
  @NonNull
  private final MenuService menuService;
  private final Cache<UUID, Long> cooldowns =
      Caffeine.newBuilder().expireAfterWrite(COOLDOWN_CACHE_SECONDS, TimeUnit.SECONDS).build();

  public void execute(
      MenuInteractionController.ClickExecutionContext context,
      @NonNull Player player,
      int rawSlot,
      @NonNull ClickType clickType,
      @NonNull ClickHandler handler) {
    if (isOnCooldown(player)) return;

    var session = resolveSession(player);
    if (session == null) return;

    var clickContext = new ClickContextImpl(session, player, rawSlot, clickType, menuService);

    playClickSound(player, context.definition(), rawSlot);

    try {
      for (var feature : context.definition().features()) {
        feature.onClick(clickContext);
      }
      handler.onClick(clickContext);
    } catch (Exception exception) {
      log.warn(
          "menu.click.handler_error menuId={} playerUuid={} slot={} handlerType={}",
          context.definition().id(),
          player.getUniqueId(),
          rawSlot,
          handler.getClass().getSimpleName(),
          exception);
    }
  }

  private MenuSession resolveSession(@NonNull Player player) {
    return menuService.getSession(player.getUniqueId()).orElse(null);
  }

  private boolean isOnCooldown(@NonNull Player player) {
    var now = System.currentTimeMillis();
    var uuid = player.getUniqueId();
    var lastClick = cooldowns.getIfPresent(uuid);
    if (lastClick != null && (now - lastClick) < DEFAULT_COOLDOWN_MS) {
      return true;
    }
    cooldowns.put(uuid, now);
    return false;
  }

  private void playClickSound(@NonNull Player player, @NonNull MenuDefinition definition, int slot) {
    if (slot < 0 || slot >= definition.slots().size()) return;
    var slotDef = definition.slots().get(slot);
    if (slotDef != null && slotDef.template() != null && slotDef.template().clickSound() != null) {
      player.playSound(
          player.getLocation(),
          slotDef.template().clickSound(),
          DEFAULT_SOUND_VOLUME,
          DEFAULT_SOUND_PITCH);
    }
  }
}
