package com.hanielfialho.menuframework.api;

import com.hanielfialho.menuframework.api.task.MenuAsyncActions;
import com.hanielfialho.menuframework.api.task.MenuTaskActions;

/**
 * Command context supplied to {@link Menu#onOpen(MenuOpenContext)}.
 *
 * <p>It can start an asynchronous operation or periodic tasks owned by the newly opened session.
 * The context is valid only during the callback and must not be retained.
 *
 * @param <S> session-state type
 */
public interface MenuOpenContext<S>
    extends MenuAsyncActions<S>, MenuTaskActions<S>, MenuNavigationContext {}
