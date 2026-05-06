package com.github.hanielcota.menuframework.builder;

import com.github.hanielcota.menuframework.api.ClickHandler;
import com.github.hanielcota.menuframework.api.DynamicContentProvider;
import com.github.hanielcota.menuframework.api.MenuFeature;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.api.PlayerInventoryClickHandler;
import com.github.hanielcota.menuframework.api.ToggleHandler;
import com.github.hanielcota.menuframework.builder.pattern.SlotPatternStrategy;
import com.github.hanielcota.menuframework.core.text.MiniMessageProvider;
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
  private Component title = Component.empty();
  private ItemTemplate fillItem;
  private PaginationConfig pagination = PaginationConfig.builder().build();
  private boolean blockPlayerInventoryClicks = true;
  private boolean blockShiftClick = true;
  private PlayerInventoryClickHandler playerInventoryClickHandler;
  private boolean built;

  public MenuBuilder(@NonNull String id, @NonNull MenuService menuService) {
    this.id = Objects.requireNonNull(id, "id");
    this.menuService = Objects.requireNonNull(menuService, "menuService");
  }

  public @NonNull MenuBuilder title(@NonNull Component title) {
    this.title = Objects.requireNonNull(title, "title");
    return this;
  }

  public @NonNull MenuBuilder title(@NonNull String miniMessage) {
    this.title =
        MiniMessageProvider.deserialize(
            Objects.requireNonNull(miniMessage, "miniMessage"));
    return this;
  }

  public @NonNull MenuBuilder rows(int rows) {
    if (rows < 1 || rows > MAX_ROWS) {
      throw new IllegalArgumentException(
          "Rows must be between 1 and " + MAX_ROWS + ", got: " + rows);
    }
    this.layout = new String[rows];
    for (int i = 0; i < rows; i++) {
      this.layout[i] = "         "; // 9 spaces
    }
    return this;
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

  public @NonNull MenuBuilder slotWithCooldown(
      int slot,
      @NonNull ItemTemplate template,
      @Nullable ClickHandler handler,
      long cooldownTicks) {
    if (slot < 0) {
      throw new IllegalArgumentException("slot cannot be negative: " + slot);
    }
    Objects.requireNonNull(template, "template");
    if (cooldownTicks < 0) {
      throw new IllegalArgumentException("cooldownTicks cannot be negative: " + cooldownTicks);
    }
    slots.put(slot, SlotDefinition.withCooldown(slot, template, handler, cooldownTicks));
    return this;
  }

  public @NonNull MenuBuilder slotWithPermission(
      int slot,
      @NonNull ItemTemplate template,
      @Nullable ClickHandler handler,
      @NonNull String permission,
      @Nullable ItemTemplate fallbackTemplate) {
    if (slot < 0) {
      throw new IllegalArgumentException("slot cannot be negative: " + slot);
    }
    Objects.requireNonNull(template, "template");
    Objects.requireNonNull(permission, "permission");
    slots.put(
        slot, SlotDefinition.withPermission(slot, template, handler, permission, fallbackTemplate));
    return this;
  }

  public @NonNull MenuBuilder toggleSlot(
      int slot,
      @NonNull ItemTemplate enabledTemplate,
      @NonNull ItemTemplate disabledTemplate,
      boolean initialState,
      @NonNull ToggleHandler toggleHandler) {
    if (slot < 0) {
      throw new IllegalArgumentException("slot cannot be negative: " + slot);
    }
    Objects.requireNonNull(enabledTemplate, "enabledTemplate");
    Objects.requireNonNull(disabledTemplate, "disabledTemplate");
    Objects.requireNonNull(toggleHandler, "toggleHandler");
    slots.put(
        slot,
        SlotDefinition.toggle(
            slot, enabledTemplate, disabledTemplate, initialState, toggleHandler));
    return this;
  }

  public @NonNull MenuBuilder fillBorder(@NonNull ItemTemplate template) {
    Objects.requireNonNull(template, "template");
    int rows = layout != null ? layout.length : 3;
    int size = Math.min(rows * COLUMNS_PER_ROW, MAX_SLOTS);

    for (int slot = 0; slot < size; slot++) {
      int row = slot / COLUMNS_PER_ROW;
      int col = slot % COLUMNS_PER_ROW;
      boolean isBorder = row == 0 || row == rows - 1 || col == 0 || col == COLUMNS_PER_ROW - 1;
      if (isBorder) {
        slots.computeIfAbsent(slot, s -> SlotDefinition.of(s, template, null));
      }
    }
    return this;
  }

  public @NonNull MenuBuilder fillEmpty(@NonNull ItemTemplate template) {
    Objects.requireNonNull(template, "template");
    this.fillItem = template;
    return this;
  }

  public @NonNull MenuBuilder fillPattern(
      @NonNull SlotPatternStrategy pattern, @NonNull ItemTemplate template) {
    Objects.requireNonNull(pattern, "pattern");
    Objects.requireNonNull(template, "template");
    int rows = layout != null ? layout.length : 3;
    int size = Math.min(rows * COLUMNS_PER_ROW, MAX_SLOTS);

    for (int slot = 0; slot < size; slot++) {
      if (pattern.matches(slot, rows)) {
        slots.computeIfAbsent(slot, s -> SlotDefinition.of(s, template, null));
      }
    }
    return this;
  }

  /**
   * @deprecated Use {@link #fillPattern(SlotPatternStrategy, ItemTemplate)} instead.
   */
  @Deprecated
  public @NonNull MenuBuilder fillPattern(
      @NonNull SlotPattern pattern, @NonNull ItemTemplate template) {
    return fillPattern((SlotPatternStrategy) pattern, template);
  }

  public @NonNull MenuBuilder pagination(@NonNull PaginationConfig pagination) {
    this.pagination = Objects.requireNonNull(pagination, "pagination");
    return this;
  }

  public @NonNull MenuBuilder feature(@NonNull MenuFeature feature) {
    this.features.add(Objects.requireNonNull(feature, "feature"));
    return this;
  }

  public @NonNull MenuBuilder allowPlayerInventoryClicks(boolean allow) {
    this.blockPlayerInventoryClicks = !allow;
    return this;
  }

  public @NonNull MenuBuilder allowShiftClick(boolean allow) {
    this.blockShiftClick = !allow;
    return this;
  }

  public @NonNull MenuBuilder onPlayerInventoryClick(@NonNull PlayerInventoryClickHandler handler) {
    this.playerInventoryClickHandler = Objects.requireNonNull(handler, "handler");
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
    if (built) {
      throw new IllegalStateException(
          "MenuBuilder has already been built. Create a new builder for each menu.");
    }
    built = true;
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
            title,
            new Int2ObjectOpenHashMap<>(slots),
            fillItem,
            pagination,
            List.copyOf(features),
            blockPlayerInventoryClicks,
            blockShiftClick,
            playerInventoryClickHandler);
    return new MenuRegistrar(
        menuService, id, definition, dynamicContentProvider, List.copyOf(staticDynamicItems));
  }

  /**
   * @deprecated Use {@link SlotPatternStrategy} implementations instead.
   */
  @Deprecated
  public enum SlotPattern implements SlotPatternStrategy {
    /** Every other slot (checkerboard pattern). */
    CHECKERBOARD {
      @Override
      public boolean matches(int slot, int rows) {
        return (slot % 2) == 0;
      }
    },
    /** All corners of the inventory. */
    CORNERS {
      @Override
      public boolean matches(int slot, int rows) {
        int cols = COLUMNS_PER_ROW;
        return slot == 0
            || slot == cols - 1
            || slot == (rows - 1) * cols
            || slot == rows * cols - 1;
      }
    },
    /** Top row only. */
    TOP_ROW {
      @Override
      public boolean matches(int slot, int rows) {
        return slot < COLUMNS_PER_ROW;
      }
    },
    /** Bottom row only. */
    BOTTOM_ROW {
      @Override
      public boolean matches(int slot, int rows) {
        return slot >= (rows - 1) * COLUMNS_PER_ROW;
      }
    }
  }
}
