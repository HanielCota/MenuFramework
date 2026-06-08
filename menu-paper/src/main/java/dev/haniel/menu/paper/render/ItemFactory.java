package dev.haniel.menu.paper.render;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.haniel.menu.item.HeadSkin;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.ItemTraits;
import dev.haniel.menu.template.IconFactory;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

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
    ItemStack item = new ItemStack(material(icon), icon.traits().amount());
    item.editMeta(meta -> decorate(meta, icon));
    return item;
  }

  private void decorate(ItemMeta meta, Icon icon) {
    meta.displayName(component(icon.name()));
    meta.lore(lore(icon));
    applyTraits(meta, icon.traits());
  }

  private void applyTraits(ItemMeta meta, ItemTraits traits) {
    if (traits.glowing()) {
      meta.setEnchantmentGlintOverride(true);
    }
    if (traits.unbreakable()) {
      meta.setUnbreakable(true);
    }
    traits.customModelData().ifPresent(value -> applyModelData(meta, value));
    applyFlags(meta, traits.flags());
    traits.head().ifPresent(head -> applyHead(meta, head));
  }

  private void applyHead(ItemMeta meta, HeadSkin head) {
    if (meta instanceof SkullMeta skull) {
      skull.setPlayerProfile(profile(head));
    }
  }

  private PlayerProfile profile(HeadSkin head) {
    return switch (head) {
      case HeadSkin.Owner owner -> ownerProfile(owner.uuid());
      case HeadSkin.Texture texture -> textureProfile(texture.base64());
    };
  }

  private PlayerProfile ownerProfile(UUID owner) {
    PlayerProfile profile = Bukkit.createProfile(owner);
    profile.completeFromCache(); // local only: fills the skin if the server knows this player
    return profile;
  }

  private PlayerProfile textureProfile(String base64) {
    PlayerProfile profile =
        Bukkit.createProfile(UUID.nameUUIDFromBytes(base64.getBytes(StandardCharsets.UTF_8)));
    profile.setProperty(new ProfileProperty("textures", base64));
    return profile;
  }

  // Legacy integer model data is broadly compatible across resource packs; the component-based
  // replacement is version-specific and the server supplies the implementation at runtime.
  @SuppressWarnings("deprecation")
  private void applyModelData(ItemMeta meta, int value) {
    meta.setCustomModelData(value);
  }

  private void applyFlags(ItemMeta meta, Set<dev.haniel.menu.item.ItemFlag> flags) {
    flags.stream().map(PaperItemFlags::toBukkit).forEach(meta::addItemFlags);
  }

  private List<Component> lore(Icon icon) {
    return icon.lore().stream().map(this::component).toList();
  }

  private Component component(String text) {
    try {
      return components.get(text, miniMessage::deserialize);
    } catch (RuntimeException malformedMiniMessage) {
      throw new IllegalArgumentException(
          "Invalid MiniMessage text: '" + text + "'", malformedMiniMessage);
    }
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
