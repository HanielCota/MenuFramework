package dev.haniel.menu.paper.render;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.template.IconFactory;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Builds an {@link ItemStack} from an {@link Icon}, deserializing MiniMessage into the display name
 * and lore.
 *
 * <p>Called once per static item or navigation control at merge time, and once per content item
 * when a page is rendered (and then cached), so a repeated page open allocates no items.
 *
 * <p>The deserialized {@link Component}s are memoized in a bounded cache: shared across every menu
 * and player for the plugin's lifetime, it must not grow without limit as reactive menus produce
 * high-cardinality, ever-changing text (counters, timers, per-player names). The material cache is
 * naturally bounded by the {@link Material} enum.
 */
public final class ItemFactory implements IconFactory<ItemStack> {

  private final MiniMessage miniMessage;
  private final Cache<String, Component> components =
      Caffeine.newBuilder().maximumSize(1024).build();
  private final ConcurrentMap<String, Material> materials = new ConcurrentHashMap<>();

  /**
   * Creates a factory using the given MiniMessage instance.
   *
   * @param miniMessage the deserializer for names and lore; never null
   */
  public ItemFactory(MiniMessage miniMessage) {
    this.miniMessage = miniMessage;
  }

  @Override
  public ItemStack create(Icon icon) {
    ItemStack item = new ItemStack(material(icon));
    item.editMeta(meta -> decorate(meta, icon));
    return item;
  }

  private void decorate(ItemMeta meta, Icon icon) {
    meta.displayName(component(icon.name()));
    meta.lore(lore(icon));
  }

  private List<Component> lore(Icon icon) {
    return icon.lore().stream().map(this::component).toList();
  }

  private Component component(String text) {
    return components.get(text, miniMessage::deserialize);
  }

  private Material material(Icon icon) {
    return materials.computeIfAbsent(icon.material(), this::material);
  }

  private Material material(String name) {
    Material material = Material.matchMaterial(name);
    if (material == null) {
      throw new IllegalArgumentException("Unknown material: " + name);
    }
    return material;
  }
}
