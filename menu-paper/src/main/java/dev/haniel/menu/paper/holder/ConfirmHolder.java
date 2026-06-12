package dev.haniel.menu.paper.holder;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.paper.api.ConfirmPrompt;
import dev.haniel.menu.paper.render.ItemFactory;
import java.util.Objects;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * The {@link org.bukkit.inventory.InventoryHolder} behind a {@link ConfirmPrompt} dialog.
 *
 * <p>Reuses the single menu listener: it is a {@link ClickableHolder}, so confirm and cancel clicks
 * route here like any menu click. A click on the confirm or cancel slot resolves the dialog, closes
 * it and runs the matching handler; closing without choosing resolves it as a cancel. A {@code
 * resolved} guard makes the programmatic close that follows a choice a no-op, so exactly one
 * handler ever runs.
 */
public final class ConfirmHolder implements ClickableHolder {

  private static final int ROWS = 3;

  private final ConfirmPrompt prompt;
  private final Inventory inventory;
  private boolean resolved;

  private ConfirmHolder(ConfirmPrompt prompt, MiniMessage miniMessage) {
    this.prompt = prompt;
    this.inventory = build(new ItemFactory(miniMessage), miniMessage);
  }

  /**
   * Opens the confirmation dialog for the player.
   *
   * @param player the viewer; never null
   * @param prompt the dialog to show; never null
   * @param miniMessage the serializer for the title; never null
   */
  public static void open(Player player, ConfirmPrompt prompt, MiniMessage miniMessage) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(prompt, "prompt");
    Objects.requireNonNull(miniMessage, "miniMessage");
    ConfirmHolder holder = new ConfirmHolder(prompt, miniMessage);
    player.openInventory(holder.inventory);
  }

  @Override
  public @NotNull Inventory getInventory() {
    return inventory;
  }

  @Override
  public void click(int rawSlot, ClickContext context) {
    if (resolved) {
      return;
    }
    if (rawSlot == prompt.confirmChoice().slot()) {
      resolve(context, prompt::runConfirm);
      return;
    }
    if (rawSlot == prompt.cancelChoice().slot()) {
      resolve(context, prompt::runCancel);
    }
  }

  /** Resolves the dialog as a cancel when it is closed without a choice. */
  public void dismissed() {
    if (resolved) {
      return;
    }
    resolved = true;
    prompt.runCancel();
  }

  private void resolve(ClickContext context, Runnable handler) {
    resolved = true;
    closeFor(context);
    handler.run();
  }

  private void closeFor(ClickContext context) {
    Player player = Bukkit.getPlayer(context.player().value());
    if (player != null) {
      player.closeInventory();
    }
  }

  private Inventory build(ItemFactory items, MiniMessage miniMessage) {
    Inventory created =
        Bukkit.createInventory(this, ROWS * 9, miniMessage.deserialize(prompt.title()));
    created.setItem(prompt.confirmChoice().slot(), items.create(prompt.confirmChoice().icon()));
    created.setItem(prompt.cancelChoice().slot(), items.create(prompt.cancelChoice().icon()));
    return created;
  }
}
