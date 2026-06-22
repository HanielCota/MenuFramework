/**
 * Core public contracts used to define and interact with menus.
 *
 * <p>Menu definitions are reusable. Per-viewer data belongs in the generic state value supplied
 * when a menu is opened. State objects should be immutable, or at minimum treated as immutable
 * after publication, because they may be captured by navigation history and asynchronous
 * transitions.
 *
 * <p>Rendering is declarative: implementations populate a complete {@link
 * com.hanielfialho.menuframework.api.MenuCanvas}. They must not mutate the underlying Bukkit
 * inventory directly.
 */
package com.hanielfialho.menuframework.api;
