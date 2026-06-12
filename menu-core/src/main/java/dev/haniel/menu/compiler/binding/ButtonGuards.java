package dev.haniel.menu.compiler.binding;

import dev.haniel.menu.action.MenuAction;
import org.jspecify.annotations.Nullable;

/**
 * The boot-resolved access rules of a {@code @Button}: an optional permission and cooldown.
 *
 * <p>Decorates a bound action with the configured guards. Permission is the outer check so a denied
 * click never consumes the cooldown; the cooldown is inner.
 *
 * <p>A single {@link Cooldown} is created once, when the guards are resolved at boot, and reused
 * for every {@link #apply(MenuAction)} call. Because a paginated button re-binds its action on each
 * open but keeps the same guards instance, the per-player window survives a menu reopen instead of
 * resetting — closing the spam-by-reopen bypass.
 */
public final class ButtonGuards {

  private static final ButtonGuards NONE = new ButtonGuards("", 0);

  private final String permission;
  private final @Nullable Cooldown cooldown;

  /**
   * Resolves the guards for a button.
   *
   * @param permission the required permission, or empty/blank for none
   * @param cooldownMillis the per-player cooldown in milliseconds, or {@code 0} for none
   */
  public ButtonGuards(String permission, long cooldownMillis) {
    this.permission = (permission == null) ? "" : permission;
    this.cooldown =
        cooldownMillis > 0 ? new Cooldown(cooldownMillis, System::currentTimeMillis) : null;
  }

  /**
   * Returns the guards for an unrestricted button.
   *
   * @return the shared empty guards
   */
  public static ButtonGuards none() {
    return NONE;
  }

  /**
   * Decorates the action with the cooldown and permission, when configured.
   *
   * @param action the bound action to guard; never null
   * @return the guarded action, or the same action when no guard applies
   */
  public MenuAction apply(MenuAction action) {
    MenuAction cooled = cooldown == null ? action : CooldownGuard.gate(action, cooldown);
    return permission.isBlank() ? cooled : PermissionGuard.require(permission, cooled);
  }
}
