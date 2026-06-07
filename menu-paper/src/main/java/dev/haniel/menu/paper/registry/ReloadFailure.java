package dev.haniel.menu.paper.registry;

import dev.haniel.menu.domain.MenuId;

/**
 * Describes one menu reload failure.
 *
 * @param id the menu that failed
 * @param message the human-readable failure reason
 */
public record ReloadFailure(MenuId id, String message) {}
