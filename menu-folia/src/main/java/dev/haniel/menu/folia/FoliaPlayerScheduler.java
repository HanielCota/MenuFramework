package dev.haniel.menu.folia;

import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.scheduler.PlayerScheduler;
import dev.haniel.menu.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Folia scheduling for one player: re-renders run on that player's region thread via their {@code
 * EntityScheduler}, the only thread allowed to touch their inventory.
 *
 * <p>If the player has logged out (or their region unloaded) the task is dropped — there is nothing
 * to re-render and no inventory to touch. The view is torn down separately on close.
 */
public final class FoliaPlayerScheduler implements PlayerScheduler {

  private final Plugin plugin;
  private final PlayerId player;

  /**
   * Creates a scheduler bound to one player.
   *
   * @param plugin the owning plugin; never null
   * @param player the player whose region runs the re-render; never null
   */
  public FoliaPlayerScheduler(Plugin plugin, PlayerId player) {
    this.plugin = plugin;
    this.player = player;
  }

  @Override
  public ScheduledTask schedule(Runnable task) {
    Player online = Bukkit.getPlayer(player.value());
    if (online == null) {
      return skipped();
    }
    return run(online, task);
  }

  private ScheduledTask run(Player online, Runnable task) {
    io.papermc.paper.threadedregions.scheduler.ScheduledTask scheduled =
        online.getScheduler().run(plugin, ignored -> task.run(), () -> {});
    return scheduled == null ? skipped() : new FoliaScheduledTask(scheduled);
  }

  private ScheduledTask skipped() {
    return new ScheduledTask() {
      @Override
      public void cancel() {}

      @Override
      public boolean scheduled() {
        return false;
      }
    };
  }
}
