package com.hanielfialho.menuframework.api.component;

import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import java.util.Objects;
import java.util.function.BiFunction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Reusable toggle button that cycles a boolean state.
 *
 * @param <S> menu-state type
 */
public final class ToggleButton<S> implements MenuComponent<S> {

  private final String slot;
  private final String label;
  private final String enabledText;
  private final String disabledText;
  private final Material enabledMaterial;
  private final Material disabledMaterial;
  private final java.util.function.Function<? super S, Boolean> reader;
  private final BiFunction<? super S, Boolean, ? extends S> writer;

  private ToggleButton(Builder<S> builder) {
    this.slot = builder.slot;
    this.label = builder.label;
    this.enabledText = builder.enabledText;
    this.disabledText = builder.disabledText;
    this.enabledMaterial = builder.enabledMaterial;
    this.disabledMaterial = builder.disabledMaterial;
    this.reader = builder.reader;
    this.writer = builder.writer;
  }

  /**
   * Starts a builder for a toggle button at a named slot.
   *
   * @param slot named slot
   * @param <S> state type
   * @return builder
   */
  public static <S> Builder<S> at(String slot) {
    return new Builder<>(slot);
  }

  @Override
  public void render(MenuRenderContext<S> context, MenuCanvas<S> canvas) {
    boolean enabled = Boolean.TRUE.equals(this.reader.apply(context.state()));
    Material material = enabled ? this.enabledMaterial : this.disabledMaterial;
    NamedTextColor color = enabled ? NamedTextColor.GREEN : NamedTextColor.GRAY;
    String text = enabled ? this.enabledText : this.disabledText;

    ItemStack icon = new ItemStack(material);
    icon.editMeta(meta -> meta.displayName(Component.text(this.label + ": " + text, color)));

    canvas.button(
        this.slot,
        icon,
        interaction -> interaction.updateState(state -> this.writer.apply(state, !enabled)));
  }

  /** Mutable builder for {@link ToggleButton}. */
  public static final class Builder<S> {

    private final String slot;
    private String label;
    private String enabledText = "on";
    private String disabledText = "off";
    private Material enabledMaterial = Material.LIME_DYE;
    private Material disabledMaterial = Material.GRAY_DYE;
    private java.util.function.Function<? super S, Boolean> reader;
    private BiFunction<? super S, Boolean, ? extends S> writer;

    private Builder(String slot) {
      this.slot = Objects.requireNonNull(slot, "slot");
    }

    public Builder<S> label(String label) {
      this.label = Objects.requireNonNull(label, "label");
      return this;
    }

    public Builder<S> enabledText(String text) {
      this.enabledText = Objects.requireNonNull(text, "text");
      return this;
    }

    public Builder<S> disabledText(String text) {
      this.disabledText = Objects.requireNonNull(text, "text");
      return this;
    }

    public Builder<S> enabledMaterial(Material material) {
      this.enabledMaterial = Objects.requireNonNull(material, "material");
      return this;
    }

    public Builder<S> disabledMaterial(Material material) {
      this.disabledMaterial = Objects.requireNonNull(material, "material");
      return this;
    }

    public Builder<S> reader(java.util.function.Function<? super S, Boolean> reader) {
      this.reader = Objects.requireNonNull(reader, "reader");
      return this;
    }

    public Builder<S> writer(BiFunction<? super S, Boolean, ? extends S> writer) {
      this.writer = Objects.requireNonNull(writer, "writer");
      return this;
    }

    public ToggleButton<S> build() {
      Objects.requireNonNull(this.label, "label must be configured");
      Objects.requireNonNull(this.reader, "reader must be configured");
      Objects.requireNonNull(this.writer, "writer must be configured");
      return new ToggleButton<>(this);
    }
  }
}
