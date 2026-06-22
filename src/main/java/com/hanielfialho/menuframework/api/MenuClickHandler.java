package com.hanielfialho.menuframework.api;

/**
 * Action associated with a menu button.
 *
 * <p>The handler runs in the viewer's entity-scheduler context. Commands recorded through the
 * interaction are applied only after the handler returns successfully; a thrown exception discards
 * the interaction buffer.
 *
 * @param <S> session-state type
 */
@FunctionalInterface
public interface MenuClickHandler<S> {

  /**
   * Handles one click on the associated button.
   *
   * @param interaction interaction valid only for this invocation
   */
  void handle(MenuInteraction<S> interaction);
}
