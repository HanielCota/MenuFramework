package dev.haniel.menu.paper.anvil;

import dev.haniel.menu.paper.api.AnvilPrompt;
import dev.haniel.menu.paper.api.AnvilPromptOpener;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.view.AnvilView;

/**
 * Opens anvil prompts and tracks the one pending per player.
 *
 * <p>Shares its pending state with {@link AnvilPromptListener}, which reads back the typed text on
 * confirm and runs the cancel path on close. A player has at most one pending prompt; it is dropped
 * on confirm, cancel or quit, so the map never retains a disconnected player.
 */
public final class AnvilPrompts implements AnvilPromptOpener {

  private final MiniMessage miniMessage;
  private final Map<UUID, AnvilPrompt<?>> pending = new ConcurrentHashMap<>();

  /**
   * Creates the runtime backed by the given serializer for prompt titles.
   *
   * @param miniMessage the serializer used for the anvil title; never null
   */
  public AnvilPrompts(MiniMessage miniMessage) {
    this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
  }

  @Override
  public void open(Player viewer, AnvilPrompt<?> prompt) {
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(prompt, "prompt");
    AnvilView view = MenuType.ANVIL.create(viewer, miniMessage.deserialize(prompt.title()));
    view.getTopInventory().setFirstItem(nameTag(prompt.initialText()));
    pending.put(viewer.getUniqueId(), prompt);
    viewer.openInventory(view);
  }

  Optional<AnvilPrompt<?>> pendingFor(UUID viewer) {
    return Optional.ofNullable(pending.get(viewer));
  }

  void forget(UUID viewer) {
    pending.remove(viewer);
  }

  private static ItemStack nameTag(String initialText) {
    ItemStack item = new ItemStack(Material.PAPER);
    if (!initialText.isEmpty()) {
      item.editMeta(meta -> meta.displayName(Component.text(initialText)));
    }
    return item;
  }
}
