package dev.haniel.menu.compiler.model;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.domain.ButtonId;

/**
 * The behaviour half of a button: its logical id bound to the action to run.
 *
 * <p>Appearance is supplied separately by the configuration and joined at merge time.
 *
 * @param id the logical id from {@code @Button}
 * @param action the action resolved from the annotated method
 */
public record ButtonBehavior(ButtonId id, MenuAction action) {}
