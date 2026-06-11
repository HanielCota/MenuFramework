package dev.haniel.menu.compiler.binding;

/**
 * A per-instance content source for a paginated menu, bound to a fresh instance at open time.
 *
 * <p>Either an eager {@link ContentProvider} (a full {@code List<MenuItem>} the framework slices)
 * or a lazy {@link PageProvider} (one {@code Page<MenuItem>} loaded per page on demand). The open
 * path pattern-matches over the two to decide whether rendering is synchronous or loads off-thread.
 */
public sealed interface BoundContent permits ContentProvider, PageProvider {}
