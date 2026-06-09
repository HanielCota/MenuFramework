package dev.haniel.menu.folia;

import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.scheduler.MenuScheduler;
import dev.haniel.menu.scheduler.PlayerScheduler;
import java.util.Objects;
import java.util.concurrent.Executor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * The Folia scheduling strategy: each player gets a scheduler bound to their region thread, so a
 * re-render never runs on a thread that does not own the player's inventory.
 */
public final class FoliaMenuScheduler implements MenuScheduler {

  private final Plugin plugin;

  /**
   * Creates a Folia scheduler for the given plugin.
   *
   * @param plugin the owning plugin; never null
   */
  public FoliaMenuScheduler(Plugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
  }

  @Override
  public PlayerScheduler forPlayer(PlayerId player) {
    return new FoliaPlayerScheduler(plugin, player);
  }

  @Override
  public Executor global() {
    return command -> Bukkit.getGlobalRegionScheduler().execute(plugin, command);
  }
}
