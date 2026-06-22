/**
 * Error reporting and observability contracts.
 *
 * <p>Error handlers may be invoked from a player entity scheduler or from an asynchronous
 * scheduler. Implementations must therefore be thread-safe and must not access Bukkit/Paper state
 * that requires an entity or region context.
 */
package com.hanielfialho.menuframework.api.error;
