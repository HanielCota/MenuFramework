package com.hanielfialho.menuframework.api.feedback;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

/** Immutable signal-to-Bukkit-sound feedback implementation. */
public final class SoundMenuFeedback implements MenuFeedback {

  private final Map<MenuFeedbackSignal, MenuSoundEffect> effects;

  private SoundMenuFeedback(Map<MenuFeedbackSignal, MenuSoundEffect> effects) {
    this.effects = Collections.unmodifiableMap(new LinkedHashMap<>(effects));
  }

  /**
   * Creates a builder.
   *
   * @return new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a conservative built-in sound profile.
   *
   * @return immutable profile
   */
  public static SoundMenuFeedback minecraftDefaults() {
    return builder()
        .sound(
            StandardMenuFeedbackSignals.BUTTON_CLICK,
            Sound.UI_BUTTON_CLICK,
            SoundCategory.MASTER,
            0.7F,
            1.0F)
        .sound(
            StandardMenuFeedbackSignals.NAVIGATION_FORWARD,
            Sound.UI_BUTTON_CLICK,
            SoundCategory.MASTER,
            0.7F,
            1.1F)
        .sound(
            StandardMenuFeedbackSignals.NAVIGATION_BACK,
            Sound.UI_BUTTON_CLICK,
            SoundCategory.MASTER,
            0.7F,
            0.9F)
        .sound(
            StandardMenuFeedbackSignals.MENU_CLOSE,
            Sound.UI_BUTTON_CLICK,
            SoundCategory.MASTER,
            0.6F,
            0.8F)
        .sound(
            StandardMenuFeedbackSignals.PAGE_PREVIOUS,
            Sound.UI_BUTTON_CLICK,
            SoundCategory.MASTER,
            0.7F,
            0.9F)
        .sound(
            StandardMenuFeedbackSignals.PAGE_NEXT,
            Sound.UI_BUTTON_CLICK,
            SoundCategory.MASTER,
            0.7F,
            1.1F)
        .sound(
            StandardMenuFeedbackSignals.ACTION_SUCCESS,
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
            SoundCategory.MASTER,
            0.7F,
            1.2F)
        .sound(
            StandardMenuFeedbackSignals.ACTION_FAILURE,
            Sound.BLOCK_NOTE_BLOCK_BASS,
            SoundCategory.MASTER,
            0.8F,
            0.8F)
        .sound(
            StandardMenuFeedbackSignals.RETRY,
            Sound.UI_BUTTON_CLICK,
            SoundCategory.MASTER,
            0.7F,
            1.0F)
        .build();
  }

  /** {@inheritDoc} */
  @Override
  public void emit(MenuFeedbackContext context, MenuFeedbackSignal signal) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(signal, "signal");

    MenuSoundEffect effect = this.effects.get(signal);
    if (effect == null) {
      return;
    }

    context
        .viewer()
        .playSound(
            context.viewer().getLocation(),
            effect.sound(),
            effect.category(),
            effect.volume(),
            effect.pitch());
  }

  /** Mutable, non-thread-safe builder for {@link SoundMenuFeedback}. */
  public static final class Builder {

    private final LinkedHashMap<MenuFeedbackSignal, MenuSoundEffect> effects =
        new LinkedHashMap<>();

    private Builder() {}

    /**
     * Associates a signal with an effect.
     *
     * @param signal feedback signal
     * @param effect sound effect
     * @return this builder
     */
    public Builder sound(MenuFeedbackSignal signal, MenuSoundEffect effect) {
      MenuFeedbackSignal checkedSignal = Objects.requireNonNull(signal, "signal");
      MenuSoundEffect checkedEffect = Objects.requireNonNull(effect, "effect");

      if (this.effects.putIfAbsent(checkedSignal, checkedEffect) != null) {
        throw new IllegalArgumentException(
            "A sound is already configured for signal: " + checkedSignal.value());
      }

      return this;
    }

    /**
     * Associates a signal with Bukkit sound parameters.
     *
     * @param signal feedback signal
     * @param sound Bukkit sound
     * @param category sound category
     * @param volume non-negative volume
     * @param pitch positive pitch
     * @return this builder
     */
    public Builder sound(
        MenuFeedbackSignal signal, Sound sound, SoundCategory category, float volume, float pitch) {
      return this.sound(signal, new MenuSoundEffect(sound, category, volume, pitch));
    }

    /**
     * Builds the immutable profile.
     *
     * @return immutable sound feedback
     * @throws IllegalStateException if no signal was configured
     */
    public SoundMenuFeedback build() {
      if (this.effects.isEmpty()) {
        throw new IllegalStateException("At least one feedback sound is required");
      }
      return new SoundMenuFeedback(this.effects);
    }
  }
}
