package com.hanielfialho.menuframework.internal.runtime;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/**
 * Maintains the one-way lifecycle state of a framework runtime.
 *
 * <p>The lifecycle monitor is intentionally held only for short registry or history mutations.
 * Bukkit/Paper calls and user callbacks must never execute while the monitor is held.
 */
public final class MenuRuntimeState {

  private final UUID runtimeId = UUID.randomUUID();
  private final Object lifecycleMonitor = new Object();
  private final AtomicBoolean shutdown = new AtomicBoolean();

  public UUID runtimeId() {
    return this.runtimeId;
  }

  /**
   * Atomically transitions this runtime to its terminal shutdown state.
   *
   * <p>The shared monitor serializes this transition with every guarded registry publication and
   * navigation-history commit.
   */
  public boolean beginShutdown() {
    synchronized (this.lifecycleMonitor) {
      return this.shutdown.compareAndSet(false, true);
    }
  }

  /**
   * Executes a short mutation only while the runtime is still accepting work.
   *
   * @param mutation mutation that does not call Bukkit/Paper APIs or user code
   * @return {@code false} when shutdown already began, otherwise the value returned by {@code
   *     mutation}
   */
  public boolean executeIfRunning(BooleanSupplier mutation) {
    Objects.requireNonNull(mutation, "mutation");

    synchronized (this.lifecycleMonitor) {
      if (this.shutdown.get()) {
        return false;
      }

      return mutation.getAsBoolean();
    }
  }

  public boolean isShutdown() {
    return this.shutdown.get();
  }

  public boolean running() {
    return !this.shutdown.get();
  }
}
