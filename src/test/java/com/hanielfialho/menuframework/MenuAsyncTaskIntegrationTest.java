package com.hanielfialho.menuframework;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
final class MenuAsyncTaskIntegrationTest extends MenuManagerTestSupport {

  @Test
  void asyncFatalCompletionIsRethrownInsteadOfRecovered() {
    AtomicBoolean failureRecoveryInvoked = new AtomicBoolean();
    LinkageError fatal = new LinkageError("fatal async");
    RecordingMenu menu = new RecordingMenu("Async fatal");

    menu.onPrimary(
        interaction ->
            interaction.executeAsync(
                MenuTaskKey.of("fatal"),
                ignored -> CompletableFuture.<Object>failedFuture(new CompletionException(fatal)),
                (state, generation) -> state.increment(),
                (state, generation, result) -> state.increment(),
                (state, generation, failure) -> {
                  failureRecoveryInvoked.set(true);
                  return state.increment();
                }));

    this.open(menu, MenuState.initial());
    this.dispatchPrimaryClick();
    this.waitAsyncTasks();

    LinkageError thrown = assertThrows(LinkageError.class, () -> this.advanceTicks(5L));

    assertSame(fatal, thrown);
    assertFalse(failureRecoveryInvoked.get());
  }
}
