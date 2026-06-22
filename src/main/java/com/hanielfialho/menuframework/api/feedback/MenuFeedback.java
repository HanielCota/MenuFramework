package com.hanielfialho.menuframework.api.feedback;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Receives transactional feedback signals requested by menu interactions.
 *
 * <p>The runtime invokes this contract on the viewer's entity-scheduler context after a click
 * handler and its synchronous state transition complete successfully. Implementations may safely
 * perform viewer-bound Bukkit operations, but must remain non-blocking.
 */
@FunctionalInterface
public interface MenuFeedback {

  /**
   * Emits one signal.
   *
   * @param context current synchronous context
   * @param signal requested signal
   */
  void emit(MenuFeedbackContext context, MenuFeedbackSignal signal);

  /**
   * Returns a feedback implementation that ignores every signal.
   *
   * @return no-op feedback
   */
  static MenuFeedback none() {
    return NoopMenuFeedback.INSTANCE;
  }

  /**
   * Combines several feedback destinations in declaration order.
   *
   * @param feedback destinations
   * @return composite feedback
   */
  static MenuFeedback composite(MenuFeedback... feedback) {
    Objects.requireNonNull(feedback, "feedback");
    List<MenuFeedback> snapshot =
        Arrays.stream(feedback)
            .map(value -> Objects.requireNonNull(value, "feedback contains null"))
            .toList();

    if (snapshot.isEmpty()) {
      return none();
    }

    return (context, signal) -> {
      for (MenuFeedback destination : snapshot) {
        destination.emit(context, signal);
      }
    };
  }

  /**
   * Returns a composite that invokes this destination before {@code next}.
   *
   * @param next next destination
   * @return composed feedback
   */
  default MenuFeedback andThen(MenuFeedback next) {
    return composite(this, Objects.requireNonNull(next, "next"));
  }
}
