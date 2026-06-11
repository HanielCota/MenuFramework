package dev.haniel.menu.paper.view;

import dev.haniel.menu.paper.refresh.RefreshSubscriber;
import dev.haniel.menu.paper.render.InventoryFactory;
import dev.haniel.menu.placeholder.PlaceholderResolver;
import dev.haniel.menu.scheduler.MenuScheduler;
import dev.haniel.menu.template.IconFactory;
import java.util.logging.Logger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;

/**
 * The platform services menus need to build views: scheduling, logging, rendering, text.
 *
 * @param logger the logger for page-cache tracing
 * @param icons the factory rendering icons into items
 * @param miniMessage the title/text deserializer
 * @param scheduler the platform scheduling strategy (Paper or Folia)
 * @param inventories the platform inventory factory
 * @param placeholders the per-viewer placeholder resolver
 * @param refreshSubscriber the {@code @RefreshOn} event subscriber for open views
 */
public record MenuRuntime(
    Logger logger,
    IconFactory<ItemStack> icons,
    MiniMessage miniMessage,
    MenuScheduler scheduler,
    InventoryFactory inventories,
    PlaceholderResolver placeholders,
    RefreshSubscriber refreshSubscriber) {

  /**
   * Creates a runtime with no placeholder resolution and no event refresh.
   *
   * @param logger the logger for page-cache tracing
   * @param icons the factory rendering icons into items
   * @param miniMessage the title/text deserializer
   * @param scheduler the platform scheduling strategy
   * @param inventories the platform inventory factory
   */
  public MenuRuntime(
      Logger logger,
      IconFactory<ItemStack> icons,
      MiniMessage miniMessage,
      MenuScheduler scheduler,
      InventoryFactory inventories) {
    this(
        logger,
        icons,
        miniMessage,
        scheduler,
        inventories,
        PlaceholderResolver.none(),
        RefreshSubscriber.none());
  }
}
