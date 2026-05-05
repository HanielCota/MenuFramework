package com.github.hanielcota.menuframework.api;

import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public interface MenuDiagnostics {

  /** Returns the plugin that owns this service instance. */
  @NonNull Plugin getPlugin();

  /** Returns runtime cache and session metrics. */
  @NonNull MenuMetrics getMetrics();

  /** Closes sessions and clears framework-owned caches. */
  void shutdown();
}
