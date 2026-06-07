package dev.haniel.menu.paper;

import dev.haniel.menu.folia.FoliaMenuScheduler;
import dev.haniel.menu.paper.scheduler.PaperMenuScheduler;
import dev.haniel.menu.scheduler.MenuScheduler;
import org.bukkit.plugin.Plugin;

final class SchedulerFactory {

  private static final boolean FOLIA = detectFolia();

  private SchedulerFactory() {}

  static MenuScheduler detect(Plugin plugin) {
    return FOLIA ? new FoliaMenuScheduler(plugin) : new PaperMenuScheduler(plugin);
  }

  private static boolean detectFolia() {
    try {
      Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
      return true;
    } catch (ClassNotFoundException notFolia) {
      return false;
    }
  }
}
