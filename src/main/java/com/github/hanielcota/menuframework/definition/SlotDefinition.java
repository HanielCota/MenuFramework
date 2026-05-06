package com.github.hanielcota.menuframework.definition;

import com.github.hanielcota.menuframework.api.ClickHandler;
import com.github.hanielcota.menuframework.api.ToggleHandler;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record SlotDefinition(
    int slot,
    @Nullable ItemTemplate template,
    @Nullable ClickHandler handler,
    boolean navigational,
    long cooldownTicks,
    @Nullable String requiredPermission,
    @Nullable ItemTemplate permissionFallbackTemplate,
    boolean toggle,
    @Nullable ToggleHandler toggleHandler,
    @Nullable ToggleState toggleStateKey) {

  private static final int MIN_SLOT = -1;
  private static final long DEFAULT_COOLDOWN = 0;

  public SlotDefinition {
    if (slot < MIN_SLOT) {
      throw new IllegalArgumentException("slot cannot be less than " + MIN_SLOT + ": " + slot);
    }
    if (cooldownTicks < 0) {
      throw new IllegalArgumentException("cooldownTicks cannot be negative: " + cooldownTicks);
    }
  }

  public static SlotDefinition of(
      int slot, @NonNull ItemTemplate template, @Nullable ClickHandler handler) {
    return slot(slot, template, handler, false, DEFAULT_COOLDOWN, null, null);
  }

  public static SlotDefinition navigational(
      int slot, @NonNull ItemTemplate template, @Nullable ClickHandler handler) {
    return slot(slot, template, handler, true, DEFAULT_COOLDOWN, null, null);
  }

  public static SlotDefinition withCooldown(
      int slot,
      @NonNull ItemTemplate template,
      @Nullable ClickHandler handler,
      long cooldownTicks) {
    return slot(slot, template, handler, false, cooldownTicks, null, null);
  }

  public static SlotDefinition withPermission(
      int slot,
      @NonNull ItemTemplate template,
      @Nullable ClickHandler handler,
      @NonNull String permission,
      @Nullable ItemTemplate fallbackTemplate) {
    return slot(
        slot,
        template,
        handler,
        false,
        DEFAULT_COOLDOWN,
        permission,
        fallbackTemplate
    );
  }

  public static SlotDefinition withHandler(int slot, @Nullable ClickHandler handler) {
    return slot(slot, null, handler, false, DEFAULT_COOLDOWN, null, null);
  }

  private static SlotDefinition slot(
      int slot,
      @Nullable ItemTemplate template,
      @Nullable ClickHandler handler,
      boolean navigational,
      long cooldownTicks,
      @Nullable String permission,
      @Nullable ItemTemplate fallbackTemplate
  ) {
    return new SlotDefinition(
        slot,
        template,
        handler,
        navigational,
        cooldownTicks,
        permission,
        fallbackTemplate,
        false,
        null,
        null
    );
  }

  public static SlotDefinition toggle(
      int slot,
      @NonNull ItemTemplate enabledTemplate,
      @NonNull ItemTemplate disabledTemplate,
      boolean initialState,
      @NonNull ToggleHandler toggleHandler) {
    Objects.requireNonNull(enabledTemplate, "enabledTemplate");
    Objects.requireNonNull(disabledTemplate, "disabledTemplate");
    Objects.requireNonNull(toggleHandler, "toggleHandler");

    return new SlotDefinition(
        slot,
        initialState ? enabledTemplate : disabledTemplate,
        null,
        false,
        DEFAULT_COOLDOWN,
        null,
        null,
        true,
        toggleHandler,
        new ToggleState(enabledTemplate, disabledTemplate, initialState));
  }
}
