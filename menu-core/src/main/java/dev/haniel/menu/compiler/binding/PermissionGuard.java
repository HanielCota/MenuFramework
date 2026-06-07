package dev.haniel.menu.compiler.binding;

import dev.haniel.menu.action.MenuAction;

/**
 * Wraps a {@link MenuAction} so it only runs when the clicker holds a permission.
 *
 * <p>Stateless, so the same guarded action is safe to share across every open view. Resolved once
 * at boot from {@code @Button(permission = ...)}; an empty permission is left unguarded by the
 * reader.
 */
public final class PermissionGuard {

  private PermissionGuard() {}

  /**
   * Returns an action that runs the delegate only when the clicker holds the permission.
   *
   * @param permission the required permission node; never blank
   * @param action the delegate to guard; never null
   * @return the guarded action
   */
  public static MenuAction require(String permission, MenuAction action) {
    return context -> {
      if (context.hasPermission(permission)) {
        action.onClick(context);
      }
    };
  }
}
