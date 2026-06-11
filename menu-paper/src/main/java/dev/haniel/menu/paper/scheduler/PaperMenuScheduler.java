package dev.haniel.menu.paper.scheduler;

import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.scheduler.MenuScheduler;
import dev.haniel.menu.scheduler.PlayerScheduler;
import java.util.Objects;
import java.util.concurrent.Executor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * The Paper scheduling strategy: every re-render runs on the global main thread, exactly as the
 * pre-Folia framework did. The per-player binding is a formality on Paper.
 */
public final class PaperMenuScheduler implements MenuScheduler {

  private final Plugin plugin;

  /**
   * Creates a Paper scheduler for the given plugin.
   *
   * @param plugin the owning plugin; never null
   */
  public PaperMenuScheduler(Plugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
  }

  @Override
  public PlayerScheduler forPlayer(PlayerId player) {
    return new PaperPlayerScheduler(plugin);
  }

  @Override
  public Executor global() {
    return command -> Bukkit.getScheduler().runTask(plugin, command);
  }

  @Override
  public Executor async() {
    return command -> Bukkit.getScheduler().runTaskAsynchronously(plugin, command);
  }
}
