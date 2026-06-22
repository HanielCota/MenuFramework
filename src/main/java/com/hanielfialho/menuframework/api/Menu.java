package com.hanielfialho.menuframework.api;

import net.kyori.adventure.text.Component;

/**
 * Reusable definition of a menu.
 *
 * <p>A menu implementation must not store viewer-specific mutable state in its fields. Session
 * state is supplied through the callback contexts and should be immutable, or at least treated as
 * immutable. A single menu instance may be shared by multiple viewers.
 *
 * <p>{@link #render(MenuRenderContext, MenuCanvas)} must describe a complete frame and must not
 * mutate the open inventory directly. Rendering, lifecycle callbacks and click handlers execute in
 * the viewer's entity-scheduler context and must not block the region thread. Use the asynchronous
 * task API exposed by the callback contexts for database, HTTP or other blocking work.
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
   * @return a non-null layout
   */
  MenuLayout layout();

  /**
   * Returns the inventory interaction policy for newly opened sessions.
   *
   * <p>The policy is evaluated once during opening.
   *
   * @return a non-null policy; defaults to {@link InteractionPolicy#READ_ONLY}
   */
  default InteractionPolicy interactionPolicy() {
    return InteractionPolicy.READ_ONLY;
  }

  /**
   * Produces the title for a newly opened inventory view.
   *
   * <p>The title is structural and is evaluated only during opening. Prefer an indicator item for
   * dynamic information such as a page number.
   *
   * @param context immutable opening snapshot
   * @return a non-null Adventure component
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
   * <p>This callback is intentionally not invoked by {@link
   * com.hanielfialho.menuframework.MenuFramework#shutdown()}: plugin shutdown has no guaranteed
   * entity-region context on Folia. Release plugin-wide resources from the owning plugin's {@code
   * onDisable()}.
   *
   * @param context final immutable session snapshot
   * @param reason logical termination reason
   */
  default void onClose(MenuContext<S> context, MenuCloseReason reason) {}
}
