package dev.haniel.menu.paper.api;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An opt-in anvil text prompt opened from a {@link MenuClick}.
 *
 * <p>Opens a single-line anvil rename field for the player. When they confirm, the typed text is
 * run through this prompt's parser: a valid value is handed to {@link #onConfirm}; an invalid one
 * leaves the anvil open so they can retype. Closing the anvil without a valid confirm runs {@link
 * #onCancel}.
 *
 * <p>The framework never reopens a menu afterwards — do that from your {@code onConfirm} with
 * {@link MenuClick#open}. This keeps navigation explicit, matching {@code open(...)}.
 *
 * <p><strong>Security:</strong> the confirmed value is the player's raw input. Never deserialize it
 * as MiniMessage without escaping it first (see {@link MenuClick#message(String)}); a title set
 * here is trusted, the typed value is not.
 *
 * @param <T> the parsed value type handed to {@link #onConfirm}
 */
public final class AnvilPrompt<T> {

  private Display display = new Display("", "");
  private Resolution<T> resolution;

  private AnvilPrompt(Function<String, Optional<T>> parser) {
    this.resolution = new Resolution<>(parser, value -> {}, () -> {});
  }

  /**
   * Starts a prompt whose confirmed value is the raw typed text.
   *
   * @return a new text prompt
   */
  public static AnvilPrompt<String> text() {
    return new AnvilPrompt<>(Optional::of);
  }

  /**
   * Starts a prompt whose confirmed value is the typed text parsed as an integer.
   *
   * <p>Non-numeric or blank input is invalid and leaves the anvil open for another try.
   *
   * @return a new numeric prompt
   */
  public static AnvilPrompt<Integer> numeric() {
    return new AnvilPrompt<>(AnvilPrompt::parseInt);
  }

  /**
   * Sets the anvil title, parsed as trusted MiniMessage.
   *
   * @param miniMessageTitle the title; never null
   * @return this prompt
   */
  public AnvilPrompt<T> title(String miniMessageTitle) {
    this.display = display.withTitle(Objects.requireNonNull(miniMessageTitle, "miniMessageTitle"));
    return this;
  }

  /**
   * Pre-fills the anvil text field with the given text.
   *
   * @param text the initial text; never null
   * @return this prompt
   */
  public AnvilPrompt<T> initialText(String text) {
    this.display = display.withInitialText(Objects.requireNonNull(text, "text"));
    return this;
  }

  /**
   * Sets the action run with the parsed value when the player confirms a valid entry.
   *
   * @param action the confirm handler; never null
   * @return this prompt
   */
  public AnvilPrompt<T> onConfirm(Consumer<T> action) {
    this.resolution = resolution.withConfirm(Objects.requireNonNull(action, "action"));
    return this;
  }

  /**
   * Sets the action run when the player closes the anvil without a valid entry.
   *
   * @param action the cancel handler; never null
   * @return this prompt
   */
  public AnvilPrompt<T> onCancel(Runnable action) {
    this.resolution = resolution.withCancel(Objects.requireNonNull(action, "action"));
    return this;
  }

  /**
   * Returns the MiniMessage title to show on the anvil.
   *
   * @return the title; never null
   */
  public String title() {
    return display.title();
  }

  /**
   * Returns the text to pre-fill the anvil with.
   *
   * @return the initial text; never null
   */
  public String initialText() {
    return display.initialText();
  }

  /**
   * Parses the typed text into a deferred confirm action.
   *
   * <p>The returned action is not run here, so the caller can close the anvil before invoking it (a
   * confirm handler may open another menu). An empty result means the input was invalid and the
   * anvil should stay open for another try.
   *
   * @param typedText the player's anvil rename text; never null
   * @return the confirm action if the value parsed, or empty to re-prompt
   */
  public Optional<Runnable> resolve(String typedText) {
    return resolution.resolve(typedText);
  }

  /** Runs the cancel handler. */
  public void cancel() {
    resolution.cancel();
  }

  private static Optional<Integer> parseInt(String raw) {
    try {
      return Optional.of(Integer.valueOf(raw.trim()));
    } catch (NumberFormatException invalid) {
      return Optional.empty();
    }
  }

  /** What the anvil shows: its title and pre-filled text. */
  private record Display(String title, String initialText) {

    Display withTitle(String newTitle) {
      return new Display(newTitle, initialText);
    }

    Display withInitialText(String newInitialText) {
      return new Display(title, newInitialText);
    }
  }

  /** How typed text becomes an outcome: parse, then confirm or cancel. */
  private record Resolution<T>(
      Function<String, Optional<T>> parser, Consumer<T> onConfirm, Runnable onCancel) {

    Optional<Runnable> resolve(String typedText) {
      return parser.apply(typedText).map(value -> () -> onConfirm.accept(value));
    }

    void cancel() {
      onCancel.run();
    }

    Resolution<T> withConfirm(Consumer<T> action) {
      return new Resolution<>(parser, action, onCancel);
    }

    Resolution<T> withCancel(Runnable action) {
      return new Resolution<>(parser, onConfirm, action);
    }
  }
}
