package com.hanielfialho.menuframework.example.menu;

import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

final class ItemStacks {

  private ItemStacks() {}

  static ItemStack named(Material material, Component name) {
    return named(material, name, List.of());
  }

  static ItemStack named(Material material, Component name, List<Component> lore) {
    Objects.requireNonNull(material, "material");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(lore, "lore");

    ItemStack item = new ItemStack(material);
    item.editMeta(
        meta -> {
          meta.displayName(notItalic(name));
          meta.lore(lore.stream().map(ItemStacks::notItalic).toList());
        });
    return item;
  }

  private static Component notItalic(Component component) {
    return component.decoration(TextDecoration.ITALIC, false);
  }
}
