package com.hanielfialho.menuframework.internal.error;

import java.util.Objects;

/**
 * Utilities for preserving fatal JVM failures when broad runtime boundaries catch {@link
 * Throwable}.
 */
public final class MenuExceptions {

  private MenuExceptions() {}

  public static void rethrowIfFatal(Throwable throwable) {
    Objects.requireNonNull(throwable, "throwable");

    if (throwable instanceof VirtualMachineError error) {
      throw error;
    }

    if (throwable instanceof LinkageError error) {
      throw error;
    }
  }
}
