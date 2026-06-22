package com.hanielfialho.menuframework.internal.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

final class MenuRuntimeStateTest {

  @Test
  void shutdownWaitsForAGuardedMutationAndRejectsLaterMutations() throws InterruptedException {
    MenuRuntimeState runtime = new MenuRuntimeState();
    CountDownLatch enteredMutation = new CountDownLatch(1);
    CountDownLatch releaseMutation = new CountDownLatch(1);
    AtomicBoolean mutationCompleted = new AtomicBoolean();
    AtomicBoolean shutdownCompleted = new AtomicBoolean();

    Thread mutationThread =
        new Thread(
            () ->
                runtime.executeIfRunning(
                    () -> {
                      enteredMutation.countDown();

                      try {
                        if (!releaseMutation.await(5L, TimeUnit.SECONDS)) {
                          throw new AssertionError("Timed out waiting to release mutation");
                        }
                      } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(exception);
                      }

                      mutationCompleted.set(true);
                      return true;
                    }));

    Thread shutdownThread =
        new Thread(
            () -> {
              runtime.beginShutdown();
              shutdownCompleted.set(true);
            });

    mutationThread.start();
    assertTrue(enteredMutation.await(5L, TimeUnit.SECONDS));

    shutdownThread.start();
    Thread.sleep(25L);
    assertFalse(shutdownCompleted.get());

    releaseMutation.countDown();
    mutationThread.join(5_000L);
    shutdownThread.join(5_000L);

    assertTrue(mutationCompleted.get());
    assertTrue(shutdownCompleted.get());
    assertTrue(runtime.isShutdown());
    assertFalse(runtime.executeIfRunning(() -> true));
  }
}
