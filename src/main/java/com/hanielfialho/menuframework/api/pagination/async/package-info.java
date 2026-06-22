/**
 * Session-safe asynchronous pagination built on keyed menu tasks.
 *
 * <p>Page sources execute outside the player entity scheduler. They should return immutable domain
 * data and must not access region-sensitive Bukkit or Paper objects.
 */
package com.hanielfialho.menuframework.api.pagination.async;
