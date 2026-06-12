package dev.haniel.menu.paper.api;

import dev.haniel.menu.item.Icon;
import java.util.Objects;

/**
 * An opt-in yes/no confirmation dialog opened from a {@link MenuClick}.
 *
 * <p>Opens a small chest with a confirm and a cancel button, sparing you a whole {@code @Menu}
 * class and YAML file for a one-off "are you sure?". Clicking confirm runs {@link #onConfirm};
 * clicking cancel — or closing the dialog — runs {@link #onCancel}. Exactly one of the two handlers
 * runs.
 *
 * <p>The framework never reopens a menu afterwards — do that from your handler with {@link
 * MenuClick#open}, matching the explicit navigation of {@code open(...)} and {@link AnvilPrompt}.
 *
 * <p>The title is parsed as trusted MiniMessage; never build it from unescaped player input (see
 * {@link MenuClick#message(String)}).
 */
public final class ConfirmPrompt {

  private Layout layout = Layout.defaults();
  private Handlers handlers = Handlers.none();

  private ConfirmPrompt() {}

  /**
   * Starts a confirmation dialog with the default title and buttons.
   *
   * @return a new prompt
   */
  public static ConfirmPrompt create() {
    return new ConfirmPrompt();
  }

  /**
   * Starts a confirmation dialog with the given title.
   *
   * @param miniMessageTitle the dialog title, parsed as trusted MiniMessage; never null
   * @return a new prompt
   */
  public static ConfirmPrompt titled(String miniMessageTitle) {
    return create().title(miniMessageTitle);
  }

  /**
   * Sets the dialog title.
   *
   * @param miniMessageTitle the title, parsed as trusted MiniMessage; never null
   * @return this prompt
   */
  public ConfirmPrompt title(String miniMessageTitle) {
    layout = layout.withTitle(Objects.requireNonNull(miniMessageTitle, "miniMessageTitle"));
    return this;
  }

  /**
   * Overrides the confirm button icon.
   *
   * @param icon the confirm icon; never null
   * @return this prompt
   */
  public ConfirmPrompt confirm(Icon icon) {
    layout = layout.withConfirm(Objects.requireNonNull(icon, "icon"));
    return this;
  }

  /**
   * Overrides the cancel button icon.
   *
   * @param icon the cancel icon; never null
   * @return this prompt
   */
  public ConfirmPrompt cancel(Icon icon) {
    layout = layout.withCancel(Objects.requireNonNull(icon, "icon"));
    return this;
  }

  /**
   * Sets the action run when the player clicks confirm.
   *
   * @param action the confirm handler; never null
   * @return this prompt
   */
  public ConfirmPrompt onConfirm(Runnable action) {
    handlers = handlers.withConfirm(Objects.requireNonNull(action, "action"));
    return this;
  }

  /**
   * Sets the action run when the player clicks cancel or closes the dialog.
   *
   * @param action the cancel handler; never null
   * @return this prompt
   */
  public ConfirmPrompt onCancel(Runnable action) {
    handlers = handlers.withCancel(Objects.requireNonNull(action, "action"));
    return this;
  }

  /**
   * Returns the MiniMessage title to show.
   *
   * @return the title; never null
   */
  public String title() {
    return layout.title();
  }

  /**
   * Returns the confirm button.
   *
   * @return the confirm choice; never null
   */
  public Choice confirmChoice() {
    return layout.confirm();
  }

  /**
   * Returns the cancel button.
   *
   * @return the cancel choice; never null
   */
  public Choice cancelChoice() {
    return layout.cancel();
  }

  /** Runs the confirm handler. */
  public void runConfirm() {
    handlers.onConfirm().run();
  }

  /** Runs the cancel handler. */
  public void runCancel() {
    handlers.onCancel().run();
  }

  /**
   * A button in the dialog: its icon and the slot it sits in.
   *
   * @param icon the rendered icon; never null
   * @param slot the slot within the dialog
   */
  public record Choice(Icon icon, int slot) {

    public Choice {
      Objects.requireNonNull(icon, "icon");
    }
  }

  /** The dialog's appearance: title and the two buttons. */
  private record Layout(String title, Choice confirm, Choice cancel) {

    static Layout defaults() {
      return new Layout(
          "<red>Are you sure?",
          new Choice(Icon.of("LIME_WOOL").named("<green>Confirm"), 11),
          new Choice(Icon.of("RED_WOOL").named("<red>Cancel"), 15));
    }

    Layout withTitle(String newTitle) {
      return new Layout(newTitle, confirm, cancel);
    }

    Layout withConfirm(Icon icon) {
      return new Layout(title, new Choice(icon, confirm.slot()), cancel);
    }

    Layout withCancel(Icon icon) {
      return new Layout(title, confirm, new Choice(icon, cancel.slot()));
    }
  }

  /** What happens on each outcome. */
  private record Handlers(Runnable onConfirm, Runnable onCancel) {

    static Handlers none() {
      return new Handlers(() -> {}, () -> {});
    }

    Handlers withConfirm(Runnable action) {
      return new Handlers(action, onCancel);
    }

    Handlers withCancel(Runnable action) {
      return new Handlers(onConfirm, action);
    }
  }
}
