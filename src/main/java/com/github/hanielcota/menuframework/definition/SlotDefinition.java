package com.github.hanielcota.menuframework.definition;

import com.github.hanielcota.menuframework.api.ClickHandler;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record SlotDefinition(
    int slot,
    @Nullable ItemTemplate template,
    @Nullable ClickHandler handler,
    boolean navigational) {

  private static final int MIN_SLOT = -1;

  public static SlotDefinition of(
      int slot, @NonNull ItemTemplate template, @Nullable ClickHandler handler) {
    if (slot < MIN_SLOT) {
      throw new IllegalArgumentException("slot cannot be less than " + MIN_SLOT + ": " + slot);
    }
    return new SlotDefinition(slot, Objects.requireNonNull(template, "template"), handler, false);
  }

  public static SlotDefinition navigational(
      int slot, @NonNull ItemTemplate template, @Nullable ClickHandler handler) {
    if (slot < MIN_SLOT) {
      throw new IllegalArgumentException("slot cannot be less than " + MIN_SLOT + ": " + slot);
    }
    return new SlotDefinition(slot, Objects.requireNonNull(template, "template"), handler, true);
  }
}
