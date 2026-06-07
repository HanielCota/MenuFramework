package dev.haniel.menu.paper.view;

import dev.haniel.menu.template.MenuTemplate;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

/**
 * An openable menu: its deserialized title paired with its pre-rendered template.
 *
 * @param title the inventory title
 * @param template the shared, pre-rendered template
 */
public record MenuView(Component title, MenuTemplate<ItemStack> template) {}
