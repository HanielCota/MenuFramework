package com.hanielfialho.menuframework.api.feedback;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuClick;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 * Immutable synchronous context supplied when feedback is emitted.
 *
 * @param sessionId current session identifier
 * @param viewer current viewer
 * @param menu current reusable menu definition
 * @param revision current positive session revision
 * @param click click that requested the feedback
 */
public record MenuFeedbackContext(
    UUID sessionId, Player viewer, Menu<?> menu, long revision, MenuClick click) {

  /** Validates all components. */
  public MenuFeedbackContext {
    Objects.requireNonNull(sessionId, "sessionId");
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(menu, "menu");
    Objects.requireNonNull(click, "click");

    if (revision <= 0L) {
      throw new IllegalArgumentException("revision must be greater than zero: " + revision);
    }
  }
}
