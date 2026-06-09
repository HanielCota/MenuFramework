package dev.haniel.menu.paper.hook;

import dev.haniel.menu.action.MenuActionException;
import java.lang.invoke.MethodHandle;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

/**
 * One resolved {@code @OnOpen}/{@code @OnClose} method handle and whether it takes the viewer.
 *
 * <p>Bound to a per-player instance to produce the {@link Consumer} the view fires on its owning
 * thread.
 */
record Handler(MethodHandle handle, boolean acceptsPlayer) {

  Consumer<Player> bind(Object instance) {
    MethodHandle bound = handle.bindTo(instance);
    return acceptsPlayer ? player -> invokeWithPlayer(bound, player) : player -> invoke(bound);
  }

  // A player-accepting handler cannot run for an absent viewer (a close fired after disconnect),
  // so it is skipped; a no-arg handler still runs and can perform viewer-independent cleanup.
  private static void invokeWithPlayer(MethodHandle bound, Player player) {
    if (player != null) {
      invoke(bound, player);
    }
  }

  @SuppressWarnings("java:S1181") // MethodHandle invocation can throw any user-declared Throwable.
  private static void invoke(MethodHandle bound, Object... args) {
    try {
      bound.invokeWithArguments(args);
    } catch (Error error) {
      throw error;
    } catch (Throwable throwable) {
      throw new MenuActionException("Lifecycle hook failed", throwable);
    }
  }
}
