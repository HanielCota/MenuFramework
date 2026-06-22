package com.hanielfialho.menuframework.api.theme;

import com.hanielfialho.menuframework.api.MenuRenderContext;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/** Built-in theme used when neither the framework nor a menu overrides visual assets. */
public final class DefaultMenuTheme implements MenuTheme {

  private static final DefaultMenuTheme INSTANCE = new DefaultMenuTheme();

  private DefaultMenuTheme() {}

  /**
   * Returns the shared stateless instance.
   *
   * @return default theme
   */
  public static DefaultMenuTheme instance() {
    return INSTANCE;
  }

  /** {@inheritDoc} */
  @Override
  public ItemStack item(MenuThemeKey key, MenuRenderContext<?> context) {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(context, "context");

    if (key.equals(StandardMenuThemeKeys.BACKGROUND)) {
      return item(Material.GRAY_STAINED_GLASS_PANE, Component.empty());
    }
    if (key.equals(StandardMenuThemeKeys.CLOSE)) {
      return item(Material.BARRIER, Component.text("Fechar", NamedTextColor.RED));
    }
    if (key.equals(StandardMenuThemeKeys.BACK_ENABLED)) {
      return item(Material.ARROW, Component.text("Voltar", NamedTextColor.YELLOW));
    }
    if (key.equals(StandardMenuThemeKeys.BACK_DISABLED)) {
      return item(Material.GRAY_DYE, Component.text("Sem menu anterior", NamedTextColor.DARK_GRAY));
    }
    if (key.equals(StandardMenuThemeKeys.PREVIOUS_ENABLED)) {
      return item(Material.ARROW, Component.text("Página anterior", NamedTextColor.YELLOW));
    }
    if (key.equals(StandardMenuThemeKeys.PREVIOUS_DISABLED)) {
      return item(Material.GRAY_DYE, Component.text("Primeira página", NamedTextColor.DARK_GRAY));
    }
    if (key.equals(StandardMenuThemeKeys.NEXT_ENABLED)) {
      return item(Material.ARROW, Component.text("Próxima página", NamedTextColor.YELLOW));
    }
    if (key.equals(StandardMenuThemeKeys.NEXT_DISABLED)) {
      return item(Material.GRAY_DYE, Component.text("Última página", NamedTextColor.DARK_GRAY));
    }
    if (key.equals(StandardMenuThemeKeys.LOADING)) {
      return item(Material.CLOCK, Component.text("Carregando...", NamedTextColor.YELLOW));
    }
    if (key.equals(StandardMenuThemeKeys.ERROR)) {
      return item(
          Material.RED_DYE,
          Component.text("Não foi possível carregar", NamedTextColor.RED),
          List.of(Component.text("Clique para tentar novamente", NamedTextColor.GRAY)));
    }
    if (key.equals(StandardMenuThemeKeys.PAGE_INDICATOR)) {
      return item(Material.PAPER, Component.text("Página", NamedTextColor.AQUA));
    }

    throw new IllegalArgumentException("Unsupported default theme key: " + key.value());
  }

  private static ItemStack item(Material material, Component name) {
    return item(material, name, List.of());
  }

  private static ItemStack item(Material material, Component name, List<Component> lore) {
    ItemStack item = new ItemStack(material);
    item.editMeta(
        meta -> {
          meta.displayName(withoutItalic(name));
          meta.lore(lore.stream().map(DefaultMenuTheme::withoutItalic).toList());
        });
    return item;
  }

  private static Component withoutItalic(Component component) {
    return component.decoration(TextDecoration.ITALIC, false);
  }
}
