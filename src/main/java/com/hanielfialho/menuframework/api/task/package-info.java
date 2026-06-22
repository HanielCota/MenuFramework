/**
 * Session-owned asynchronous and periodic task contracts.
 *
 * <p>Tasks are identified by stable keys. Starting another task with the same key replaces the
 * previous generation, while every late result is validated against both the owning session and the
 * active generation.
 */
package com.hanielfialho.menuframework.api.task;
