package com.hanielfialho.menuframework.testing;

import com.hanielfialho.menuframework.api.feedback.MenuFeedbackSignal;
import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable result of one click executed by {@link MenuTestHarness}.
 *
 * @param state state after synchronous processing
 * @param revision harness revision after synchronous processing
 * @param rendered whether a new frame was published
 * @param closeRequested whether the handler requested close
 * @param backRequested whether the handler requested back navigation
 * @param navigation optional forward-navigation request
 * @param asyncTask optional asynchronous task key whose start transition was applied
 * @param periodicTasks periodic task keys requested by the handler
 * @param cancelledTasks task keys requested for cancellation
 * @param feedbackSignals transactional feedback signals requested by the handler
 * @param <S> menu-state type
 */
public record MenuTestOutcome<S>(
    S state,
    long revision,
    boolean rendered,
    boolean closeRequested,
    boolean backRequested,
    Optional<MenuTestNavigation> navigation,
    Optional<MenuTaskKey> asyncTask,
    List<MenuTaskKey> periodicTasks,
    List<MenuTaskKey> cancelledTasks,
    List<MenuFeedbackSignal> feedbackSignals) {

  /** Validates and defensively copies all values. */
  public MenuTestOutcome {
    Objects.requireNonNull(state, "state");
    Objects.requireNonNull(navigation, "navigation");
    Objects.requireNonNull(asyncTask, "asyncTask");
    periodicTasks = List.copyOf(periodicTasks);
    cancelledTasks = List.copyOf(cancelledTasks);
    feedbackSignals = List.copyOf(feedbackSignals);

    if (revision <= 0L) {
      throw new IllegalArgumentException("revision must be greater than zero: " + revision);
    }
  }
}
