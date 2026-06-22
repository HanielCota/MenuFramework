package com.hanielfialho.menuframework.internal.platform;

import com.hanielfialho.menuframework.internal.runtime.MenuRuntimeState;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** Fronteira única entre o runtime e os schedulers Paper/Folia. */
public final class MenuScheduler {

  private final Plugin plugin;
  private final MenuRuntimeState runtimeState;

  public MenuScheduler(Plugin plugin, MenuRuntimeState runtimeState) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
    this.runtimeState = Objects.requireNonNull(runtimeState, "runtimeState");
  }

  public boolean available() {
    return this.runtimeState.running() && this.plugin.isEnabled();
  }

  public boolean execute(Player viewer, Runnable action) {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(action, "action");

    if (!this.available()) {
      return false;
    }

    try {
      ScheduledTask task =
          viewer
              .getScheduler()
              .run(
                  this.plugin,
                  ignored -> {
                    if (this.runtimeState.running()) {
                      action.run();
                    }
                  },
                  null);

      return task != null;
    } catch (IllegalStateException ignored) {
      return false;
    }
  }

  public ScheduledTask runAsyncNow(Consumer<ScheduledTask> action) {
    Objects.requireNonNull(action, "action");

    if (!this.available()) {
      return null;
    }

    try {
      return this.plugin.getServer().getAsyncScheduler().runNow(this.plugin, action);
    } catch (IllegalStateException ignored) {
      return null;
    }
  }

  public ScheduledTask run(Player viewer, Consumer<ScheduledTask> action, Runnable retired) {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(action, "action");

    if (!this.available()) {
      return null;
    }

    try {
      return viewer.getScheduler().run(this.plugin, action, retired);
    } catch (IllegalStateException ignored) {
      return null;
    }
  }

  public ScheduledTask runAtFixedRate(
      Player viewer,
      Consumer<ScheduledTask> action,
      Runnable retired,
      long initialDelayTicks,
      long periodTicks) {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(action, "action");

    if (!this.available()) {
      return null;
    }

    try {
      return viewer
          .getScheduler()
          .runAtFixedRate(this.plugin, action, retired, initialDelayTicks, periodTicks);
    } catch (IllegalStateException ignored) {
      return null;
    }
  }
}
