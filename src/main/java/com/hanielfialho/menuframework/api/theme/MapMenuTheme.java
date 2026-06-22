package com.hanielfialho.menuframework.api.theme;

import com.hanielfialho.menuframework.api.MenuRenderContext;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

/** Immutable map-backed theme with an optional fallback theme. */
public final class MapMenuTheme implements MenuTheme {

  private final Map<MenuThemeKey, MenuThemeItemFactory> items;
  private final MenuTheme fallback;

  private MapMenuTheme(Map<MenuThemeKey, MenuThemeItemFactory> items, MenuTheme fallback) {
    this.items = Collections.unmodifiableMap(new LinkedHashMap<>(items));
    this.fallback = fallback;
  }

  /**
   * Creates a theme builder.
   *
   * @return new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** {@inheritDoc} */
  @Override
  public ItemStack item(MenuThemeKey key, MenuRenderContext<?> context) {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(context, "context");

    MenuThemeItemFactory factory = this.items.get(key);

    if (factory != null) {
      return copyAndValidate(factory.create(context), key);
    }

    if (this.fallback != null) {
      return copyAndValidate(this.fallback.item(key, context), key);
    }

    throw new IllegalArgumentException("Unsupported menu theme key: " + key.value());
  }

  private static ItemStack copyAndValidate(ItemStack item, MenuThemeKey key) {
    ItemStack checked =
        Objects.requireNonNull(item, "Theme returned null for key '" + key.value() + "'");

    if (checked.getType().isAir() || checked.getAmount() <= 0) {
      throw new IllegalArgumentException(
          "Theme returned an invalid icon for key '" + key.value() + "'");
    }

    return checked.clone();
  }

  /** Mutable, non-thread-safe builder for {@link MapMenuTheme}. */
  public static final class Builder {

    private final LinkedHashMap<MenuThemeKey, MenuThemeItemFactory> items = new LinkedHashMap<>();
    private MenuTheme fallback;

    private Builder() {}

    /**
     * Installs a fixed icon template.
     *
     * @param key icon key
     * @param item icon template
     * @return this builder
     */
    public Builder item(MenuThemeKey key, ItemStack item) {
      MenuThemeKey checkedKey = Objects.requireNonNull(key, "key");
      ItemStack snapshot = copyAndValidate(item, checkedKey);
      return this.item(checkedKey, context -> snapshot.clone());
    }

    /**
     * Installs a dynamic icon factory.
     *
     * @param key icon key
     * @param factory icon factory
     * @return this builder
     */
    public Builder item(MenuThemeKey key, MenuThemeItemFactory factory) {
      MenuThemeKey checkedKey = Objects.requireNonNull(key, "key");
      MenuThemeItemFactory checkedFactory = Objects.requireNonNull(factory, "factory");

      if (this.items.putIfAbsent(checkedKey, checkedFactory) != null) {
        throw new IllegalArgumentException(
            "Theme key has already been configured: " + checkedKey.value());
      }

      return this;
    }

    /**
     * Sets a fallback used for keys not present in this map.
     *
     * @param fallback fallback theme
     * @return this builder
     */
    public Builder fallback(MenuTheme fallback) {
      this.fallback = Objects.requireNonNull(fallback, "fallback");
      return this;
    }

    /**
     * Builds the immutable theme.
     *
     * @return immutable theme
     * @throws IllegalStateException if neither items nor a fallback were configured
     */
    public MapMenuTheme build() {
      if (this.items.isEmpty() && this.fallback == null) {
        throw new IllegalStateException("A theme requires at least one item or a fallback");
      }

      return new MapMenuTheme(this.items, this.fallback);
    }
  }
}
