package dev.haniel.menu.compiler.binding;

import dev.haniel.menu.action.MenuAction;
import java.util.function.LongSupplier;

/**
 * Wraps a {@link MenuAction} so it runs at most once per cooldown window, per player.
 *
 * <p>Each wrapped action owns its own {@link Cooldown} state, so a shared static action keeps a
 * per-player window across reopens while a per-open paginated action resets. A click made while
 * cooling down is silently dropped.
 */
public final class CooldownGuard {

  private CooldownGuard() {}

  /**
   * Wraps the action with a cooldown driven by the system clock.
   *
   * @param action the delegate to gate; never null
   * @param cooldownMillis the window length in milliseconds; must be {@code >= 1}
   * @return the gated action
   */
  public static MenuAction wrap(MenuAction action, long cooldownMillis) {
    return wrap(action, cooldownMillis, System::currentTimeMillis);
  }

  /**
   * Wraps the action with a cooldown driven by the given clock.
   *
   * @param action the delegate to gate; never null
   * @param cooldownMillis the window length in milliseconds; must be {@code >= 1}
   * @param clock the millisecond time source; never null
   * @return the gated action
   */
  public static MenuAction wrap(MenuAction action, long cooldownMillis, LongSupplier clock) {
    return gate(action, new Cooldown(cooldownMillis, clock));
  }

  /**
   * Wraps the action with an already-built cooldown, so callers can share one cooldown instance
   * across every binding of the same button. Sharing is what keeps the per-player window alive
   * across menu reopens instead of resetting on each open.
   *
   * @param action the delegate to gate; never null
   * @param cooldown the shared per-player cooldown state; never null
   * @return the gated action
   */
  public static MenuAction gate(MenuAction action, Cooldown cooldown) {
    return context -> {
      if (cooldown.tryAcquire(context.player())) {
        action.onClick(context);
      }
    };
  }
}
