package com.hanielfialho.menuframework.api;

import com.hanielfialho.menuframework.MenuFramework;
import com.hanielfialho.menuframework.api.feedback.MenuFeedback;
import com.hanielfialho.menuframework.api.theme.MenuTheme;
import java.util.Objects;
import net.kyori.adventure.text.Component;

/**
 * Reusable definition of a menu.
 *
 * <p>A menu implementation must not store viewer-specific mutable state in its fields. Session
 * state is supplied through callback contexts and should be immutable, or at least treated as
 * immutable. A single menu instance may be shared by multiple viewers.
 *
 * <p>{@link #render(MenuRenderContext, MenuCanvas)} must describe a complete frame and must not
 * mutate the open inventory directly. Rendering, lifecycle callbacks and click handlers execute in
 * the viewer's entity-scheduler context and must not block the region thread. Use the asynchronous
 * task API exposed by callback contexts for database, HTTP or other blocking work.
 *
 * @param <S> immutable session-state type
 */
public interface Menu<S> {

  /**
   * Returns the structural layout of this menu.
   *
   * <p>The layout is captured when a session opens and cannot change while that session remains
   * open.
   *
   * @return non-null layout
   */
  MenuLayout layout();

  /**
   * Returns the inventory interaction policy for newly opened sessions.
   *
   * @return non-null policy; defaults to {@link InteractionPolicy#READ_ONLY}
   */
  default InteractionPolicy interactionPolicy() {
    return InteractionPolicy.READ_ONLY;
  }

  /**
   * Resolves the immutable theme captured by a newly opened session.
   *
   * <p>Override this method to replace or decorate the framework-level default theme for this menu.
   * The returned theme is resolved once per opening and reused by every subsequent render of that
   * session.
   *
   * @param frameworkTheme configured framework-level theme
   * @return non-null theme for the session
   */
  default MenuTheme theme(MenuTheme frameworkTheme) {
    return Objects.requireNonNull(frameworkTheme, "frameworkTheme");
  }

  /**
   * Resolves the feedback destination captured by a newly opened session.
   *
   * <p>Override this method to replace or compose the framework-level feedback destination for a
   * particular menu.
   *
   * @param frameworkFeedback configured framework-level feedback
   * @return non-null feedback destination
   */
  default MenuFeedback feedback(MenuFeedback frameworkFeedback) {
    return Objects.requireNonNull(frameworkFeedback, "frameworkFeedback");
  }

  /**
   * Produces the title for a newly opened inventory view.
   *
   * <p>The title is structural and is evaluated only during opening. Prefer an indicator item for
   * dynamic information such as a page number.
   *
   * @param context immutable opening snapshot
   * @return non-null Adventure component
   */
  Component title(MenuRenderContext<S> context);

  /**
   * Describes a complete frame using the supplied canvas.
   *
   * <p>Unassigned slots remain empty unless a background is configured. Assigning the same slot
   * more than once in one render is a programming error and fails the render before the frame is
   * published.
   *
   * @param context immutable render snapshot
   * @param canvas mutable canvas valid only for this invocation
   */
  void render(MenuRenderContext<S> context, MenuCanvas<S> canvas);

  /**
   * Called once after the inventory view has opened and been validated.
   *
   * <p>The supplied context accepts task commands only while this callback is running. Do not
   * retain it.
   *
   * @param context opening context for the new session
   */
  default void onOpen(MenuOpenContext<S> context) {}

  /**
   * Called once when an opened session terminates normally.
   *
   * <p>This callback is intentionally not invoked by {@link MenuFramework#shutdown()}: plugin
   * shutdown has no guaranteed entity-region context on Folia. Release plugin-wide resources from
   * the owning plugin's {@code onDisable()}.
   *
   * @param context final immutable session snapshot
   * @param reason logical termination reason
   */
  default void onClose(MenuContext<S> context, MenuCloseReason reason) {}
}
