package dev.haniel.menu.paper;

import dev.haniel.menu.folia.FoliaMenuScheduler;
import dev.haniel.menu.paper.scheduler.PaperMenuScheduler;
import dev.haniel.menu.scheduler.MenuScheduler;
import org.bukkit.plugin.Plugin;

final class SchedulerFactory {

  private SchedulerFactory() {}

  static MenuScheduler detect(Plugin plugin) {
    return isFolia() ? new FoliaMenuScheduler(plugin) : new PaperMenuScheduler(plugin);
  }

  private static boolean isFolia() {
    try {
      Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
      return true;
    } catch (ClassNotFoundException notFolia) {
      return false;
    }
  }
}
