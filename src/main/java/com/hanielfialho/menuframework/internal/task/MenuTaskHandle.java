package com.hanielfialho.menuframework.internal.task;

import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Controle interno de uma operação pertencente a uma MenuSession.
 *
 * <p>O cancelamento é best effort: ScheduledTask não interrompe uma execução que já começou, e
 * CompletableFuture pode não propagar o cancelamento para a operação subjacente. A validação de
 * sessão + handle continua sendo a proteção definitiva contra resultados atrasados.
 */
public final class MenuTaskHandle {

  private final MenuTaskKey key;
  private final long generation;
  private final AtomicReference<Status> status = new AtomicReference<>(Status.ACTIVE);
  private final Set<ScheduledTask> scheduledTasks = ConcurrentHashMap.newKeySet();
  private final AtomicReference<CompletableFuture<?>> completionFuture = new AtomicReference<>();

  public MenuTaskHandle(MenuTaskKey key, long generation) {
    this.key = Objects.requireNonNull(key, "key");

    if (generation <= 0L) {
      throw new IllegalArgumentException("generation must be greater than zero: " + generation);
    }

    this.generation = generation;
  }

  private static boolean isTerminal(ScheduledTask task) {
    try {
      return switch (task.getExecutionState()) {
        case FINISHED, CANCELLED, CANCELLED_RUNNING -> true;
        default -> false;
      };
    } catch (RuntimeException ignored) {
      /*
       * Se a implementação não puder informar o estado, mantemos o
       * task registrado para que cancel() ainda possa alcançá-lo.
       */
      return false;
    }
  }

  private static void cancelScheduledTask(ScheduledTask task) {
    try {
      task.cancel();
    } catch (RuntimeException ignored) {
      // Cleanup best effort; a validação de identidade permanece ativa.
    }
  }

  private static void cancelFuture(CompletableFuture<?> future) {
    try {
      future.cancel(true);
    } catch (RuntimeException ignored) {
      // CompletionStage pode não suportar cancelamento propagado.
    }
  }

  public MenuTaskKey key() {
    return this.key;
  }

  public long generation() {
    return this.generation;
  }

  public boolean active() {
    return this.status.get() == Status.ACTIVE;
  }

  public void trackScheduledTask(ScheduledTask task) {
    Objects.requireNonNull(task, "task");

    if (!this.active()) {
      cancelScheduledTask(task);
      return;
    }

    if (isTerminal(task)) {
      return;
    }

    this.scheduledTasks.add(task);

    /*
     * Cobre a corrida em que cancel() ocorre entre a primeira
     * verificação e a inclusão no conjunto.
     */
    if (!this.active()) {
      if (this.scheduledTasks.remove(task)) {
        cancelScheduledTask(task);
      }
      return;
    }

    /*
     * runNow() pode terminar antes de devolver seu ScheduledTask.
     * Nesse caso, não mantemos uma referência inútil até o fim da sessão.
     */
    if (isTerminal(task)) {
      this.scheduledTasks.remove(task);
    }
  }

  public void untrackScheduledTask(ScheduledTask task) {
    Objects.requireNonNull(task, "task");
    this.scheduledTasks.remove(task);
  }

  public void trackCompletionFuture(CompletableFuture<?> future) {
    Objects.requireNonNull(future, "future");

    if (!this.completionFuture.compareAndSet(null, future)) {
      throw new IllegalStateException(
          "A completion future is already registered "
              + "for task '"
              + this.key.value()
              + "' generation "
              + this.generation);
    }

    /*
     * Cobre cancelamento ocorrido antes ou durante o bind do future.
     */
    if (!this.active() && this.completionFuture.compareAndSet(future, null)) {
      cancelFuture(future);
    }
  }

  public void untrackCompletionFuture(CompletableFuture<?> future) {
    Objects.requireNonNull(future, "future");

    this.completionFuture.compareAndSet(future, null);
  }

  public void cancel() {
    if (!this.status.compareAndSet(Status.ACTIVE, Status.CANCELLED)) {
      return;
    }

    this.cancelTrackedResources();
  }

  public boolean complete() {
    if (!this.status.compareAndSet(Status.ACTIVE, Status.COMPLETED)) {
      return false;
    }

    /*
     * Na conclusão normal, os recursos já terminaram ou estão executando
     * o callback atual. Apenas removemos as referências.
     */
    this.scheduledTasks.clear();
    this.completionFuture.set(null);

    return true;
  }

  private void cancelTrackedResources() {
    CompletableFuture<?> future = this.completionFuture.getAndSet(null);

    if (future != null) {
      cancelFuture(future);
    }

    for (ScheduledTask task : this.scheduledTasks) {
      cancelScheduledTask(task);
    }

    this.scheduledTasks.clear();
  }

  private enum Status {
    ACTIVE,
    CANCELLED,
    COMPLETED
  }
}
