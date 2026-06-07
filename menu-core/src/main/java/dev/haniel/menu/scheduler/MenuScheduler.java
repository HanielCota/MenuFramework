package dev.haniel.menu.scheduler;

import dev.haniel.menu.domain.PlayerId;
import java.util.concurrent.Executor;

/**
 * The scheduling strategy a platform provides to drive coalesced re-renders.
 *
 * <p>The core defines this contract and knows nothing of Bukkit or Folia. Paper and Folia each
 * supply an implementation; the chosen one is injected at boot so the rest of the framework is
 * platform-agnostic.
 */
public interface MenuScheduler {

  /**
   * Returns a scheduler bound to the given player's context.
   *
   * @param player the owner of the view to re-render; never null
   * @return a player-scoped scheduler
   */
  PlayerScheduler forPlayer(PlayerId player);

  /**
   * Returns an executor that runs tasks on the platform's global tick context — the main thread on
   * Paper, the global region thread on Folia. Used for work that touches the Bukkit API but is not
   * tied to a single player (e.g. applying a reloaded menu). Routing it through the platform avoids
   * the legacy {@code Bukkit.getScheduler()}, which is unsupported on Folia.
   *
   * @return the global executor; never null
   */
  Executor global();
}
