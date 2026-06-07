package dev.haniel.menu.compiler.model;

/**
 * Handles every compiled menu variant without forcing callers to switch on concrete record types.
 *
 * @param <V> the platform visual type
 * @param <R> the result type
 */
public interface CompiledMenuVisitor<V, R> {

  /**
   * Handles a compiled static menu.
   *
   * @param menu the static menu
   * @return the visitor result
   */
  R visitStatic(CompiledStaticMenu<V> menu);

  /**
   * Handles a compiled paginated menu.
   *
   * @param menu the paginated menu
   * @return the visitor result
   */
  R visitPaged(CompiledPagedMenu<V> menu);
}
