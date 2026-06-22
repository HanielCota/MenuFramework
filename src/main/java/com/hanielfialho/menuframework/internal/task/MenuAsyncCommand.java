package com.hanielfialho.menuframework.internal.task;

import com.hanielfialho.menuframework.api.task.MenuAsyncActions;
import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import java.util.Objects;

/** Comando interno que agrega uma operação assíncrona e suas transições. */
public record MenuAsyncCommand<S, R>(
    MenuTaskKey key,
    MenuAsyncActions.Operation<R> operation,
    MenuAsyncActions.Start<S> onStart,
    MenuAsyncActions.Success<S, R> onSuccess,
    MenuAsyncActions.Failure<S> onFailure) {

  public MenuAsyncCommand {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(operation, "operation");
    Objects.requireNonNull(onStart, "onStart");
    Objects.requireNonNull(onSuccess, "onSuccess");
    Objects.requireNonNull(onFailure, "onFailure");
  }
}
