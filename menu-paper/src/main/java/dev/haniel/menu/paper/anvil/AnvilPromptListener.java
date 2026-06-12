package dev.haniel.menu.paper.anvil;

import dev.haniel.menu.paper.api.AnvilPrompt;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;

/**
 * Drives the anvil text prompt: shows a clickable result, captures the typed text on confirm and
 * runs the cancel path on close.
 *
 * <p>Every click in a pending anvil is cancelled so the placeholder item can never be taken; only a
 * click on the result slot confirms. Invalid input leaves the anvil open (the click is simply
 * cancelled); a valid one closes the anvil and runs the confirm handler afterwards, so the handler
 * may open another menu cleanly.
 */
public final class AnvilPromptListener implements Listener {

  private static final int RESULT_SLOT = 2;

  private final AnvilPrompts prompts;

  /**
   * Creates a listener over the given prompt runtime.
   *
   * @param prompts the shared prompt state; never null
   */
  public AnvilPromptListener(AnvilPrompts prompts) {
    this.prompts = Objects.requireNonNull(prompts, "prompts");
  }

  /**
   * Forces a clickable result item so the player can confirm even without a real rename recipe.
   *
   * @param event the anvil preparation event
   */
  @EventHandler
  public void onPrepare(PrepareAnvilEvent event) {
    AnvilView view = event.getView();
    if (prompts.pendingFor(view.getPlayer().getUniqueId()).isPresent()) {
      event.setResult(resultItem(view.getRenameText()));
      view.setRepairCost(0);
    }
  }

  /**
   * Cancels every click in a pending anvil and confirms on the result slot.
   *
   * @param event the click event
   */
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onClick(InventoryClickEvent event) {
    if (!(event.getView() instanceof AnvilView view)) {
      return;
    }
    Optional<AnvilPrompt<?>> pending = prompts.pendingFor(event.getWhoClicked().getUniqueId());
    if (pending.isEmpty()) {
      return;
    }
    event.setCancelled(true);
    if (event.getRawSlot() == RESULT_SLOT && event.getWhoClicked() instanceof Player player) {
      confirm(view, pending.get(), player);
    }
  }

  /**
   * Cancels drags that touch a pending anvil's slots, so no item can be deposited into the inputs
   * and destroyed by the confirm/close clears.
   *
   * @param event the drag event
   */
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onDrag(InventoryDragEvent event) {
    if (!(event.getView() instanceof AnvilView)) {
      return;
    }
    boolean pending = prompts.pendingFor(event.getWhoClicked().getUniqueId()).isPresent();
    if (pending && touchesAnvilSlots(event)) {
      event.setCancelled(true);
    }
  }

  private boolean touchesAnvilSlots(InventoryDragEvent event) {
    return event.getRawSlots().stream().anyMatch(slot -> slot <= RESULT_SLOT);
  }

  /**
   * Runs the cancel path if a pending anvil is closed without a valid confirm.
   *
   * @param event the close event
   */
  @EventHandler
  public void onClose(InventoryCloseEvent event) {
    if (!(event.getView() instanceof AnvilView)) {
      return;
    }
    UUID viewer = event.getPlayer().getUniqueId();
    prompts
        .pendingFor(viewer)
        .ifPresent(
            prompt -> {
              prompts.forget(viewer);
              event.getInventory().clear();
              prompt.cancel();
            });
  }

  /**
   * Drops a disconnecting player's pending prompt.
   *
   * @param event the quit event
   */
  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    prompts.forget(event.getPlayer().getUniqueId());
  }

  private void confirm(AnvilView view, AnvilPrompt<?> prompt, Player player) {
    String typed = view.getRenameText();
    Optional<Runnable> action = prompt.resolve(typed == null ? "" : typed);
    if (action.isEmpty()) {
      return; // invalid: the click is already cancelled, so the anvil stays open
    }
    prompts.forget(player.getUniqueId());
    view.getTopInventory().clear();
    player.closeInventory();
    action.get().run();
  }

  private static ItemStack resultItem(String renameText) {
    String label = (renameText == null || renameText.isBlank()) ? " " : renameText;
    ItemStack result = new ItemStack(Material.PAPER);
    result.editMeta(meta -> meta.displayName(Component.text(label)));
    return result;
  }
}
