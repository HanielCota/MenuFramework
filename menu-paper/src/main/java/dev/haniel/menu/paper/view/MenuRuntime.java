package dev.haniel.menu.paper.view;

import dev.haniel.menu.paper.render.InventoryFactory;
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
 */
public record MenuRuntime(
    Logger logger,
    IconFactory<ItemStack> icons,
    MiniMessage miniMessage,
    MenuScheduler scheduler,
    InventoryFactory inventories) {}
