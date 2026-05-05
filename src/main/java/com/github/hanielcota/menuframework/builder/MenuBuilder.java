package com.github.hanielcota.menuframework.builder;

import com.github.hanielcota.menuframework.api.ClickHandler;
import com.github.hanielcota.menuframework.api.DynamicContentProvider;
import com.github.hanielcota.menuframework.api.MenuFeature;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.definition.MenuDefinition;
import com.github.hanielcota.menuframework.definition.PaginationConfig;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class MenuBuilder {

  private static final int COLUMNS_PER_ROW = 9;
  private static final int MAX_ROWS = 6;
  private static final int MAX_SLOTS = MAX_ROWS * COLUMNS_PER_ROW;
  private static final int DEFAULT_SLOTS = 3 * COLUMNS_PER_ROW;

  @NonNull private final String id;
  private final MenuService menuService;
  private final Int2ObjectOpenHashMap<SlotDefinition> slots = new Int2ObjectOpenHashMap<>();
  private final List<MenuFeature> features = new ArrayList<>();
  private final List<SlotDefinition> staticDynamicItems = new ArrayList<>();
  private final Map<Character, SlotDefinition> layoutBindings = new HashMap<>();
  private DynamicContentProvider dynamicContentProvider;
  private String[] layout;

  public MenuBuilder(@NonNull String id, @NonNull MenuService menuService) {
    this.id = Objects.requireNonNull(id, "id");
    this.menuService = Objects.requireNonNull(menuService, "menuService");
  }

  public @NonNull MenuBuilder layout(@NonNull String... layout) {
    this.layout = Objects.requireNonNull(layout, "layout");
    return this;
  }

  public @NonNull MenuBuilder bind(char character, @NonNull ItemTemplate template) {
    Objects.requireNonNull(template, "template");
    return bind(character, template, null);
  }

  public @NonNull MenuBuilder bind(
      char character, @NonNull ItemTemplate template, @Nullable ClickHandler handler) {
    Objects.requireNonNull(template, "template");
    layoutBindings.put(character, SlotDefinition.of(-1, template, handler));
    return this;
  }

  public @NonNull MenuBuilder bindNavigational(char character, @NonNull ItemTemplate template) {
    Objects.requireNonNull(template, "template");
    layoutBindings.put(character, SlotDefinition.navigational(-1, template, null));
    return this;
  }

  private void applyLayout() {
    if (layout == null) return;

    for (int row = 0; row < layout.length; row++) {
      String rowString = layout[row];
      if (rowString == null) continue;
      int columns = Math.min(rowString.length(), COLUMNS_PER_ROW);

      for (int col = 0; col < columns; col++) {
        char character = rowString.charAt(col);
        if (character == ' ') continue;

        var binding = layoutBindings.get(character);
        if (binding == null) continue;

        int slot = row * COLUMNS_PER_ROW + col;
        applyBindingToSlot(slot, binding);
      }
    }
  }

  private void applyBindingToSlot(int slot, @NonNull SlotDefinition binding) {
    if (binding.navigational()) {
      navigational(slot, binding.template(), binding.handler());
      return;
    }
    slot(slot, binding.template(), binding.handler());
  }

  public @NonNull MenuBuilder slot(
      int slot, @Nullable ItemTemplate template, @Nullable ClickHandler handler) {
    if (slot < 0) {
      throw new IllegalArgumentException("slot cannot be negative: " + slot);
    }
    if (template != null) {
      slots.put(slot, SlotDefinition.of(slot, template, handler));
    }
    return this;
  }

  public @NonNull MenuBuilder navigational(
      int slot, @Nullable ItemTemplate template, @Nullable ClickHandler handler) {
    if (slot < 0) {
      throw new IllegalArgumentException("slot cannot be negative: " + slot);
    }
    if (template != null) {
      slots.put(slot, SlotDefinition.navigational(slot, template, handler));
    }
    return this;
  }

  public @NonNull MenuBuilder dynamicContent(@NonNull DynamicContentProvider provider) {
    this.dynamicContentProvider = Objects.requireNonNull(provider, "provider");
    return this;
  }

  public @NonNull MenuBuilder addItem(
      @NonNull ItemTemplate template, @Nullable ClickHandler handler) {
    this.staticDynamicItems.add(
        SlotDefinition.of(-1, Objects.requireNonNull(template, "template"), handler));
    return this;
  }

  public @NonNull MenuRegistrar build() {
    applyLayout();
    int size =
        layout != null ? Math.min(layout.length * COLUMNS_PER_ROW, MAX_SLOTS) : DEFAULT_SLOTS;
    size = Math.max(size, COLUMNS_PER_ROW);
    if (layout != null && layout.length > MAX_ROWS) {
      throw new IllegalArgumentException(
          "Layout cannot exceed " + MAX_ROWS + " rows, got: " + layout.length);
    }
    var definition =
        new MenuDefinition(
            id,
            InventoryType.CHEST,
            size,
            Component.empty(),
            new Int2ObjectOpenHashMap<>(slots),
            null,
            PaginationConfig.builder().build(),
            List.copyOf(features),
            true,
            true);
    return new MenuRegistrar(
        menuService, id, definition, dynamicContentProvider, List.copyOf(staticDynamicItems));
  }
}
