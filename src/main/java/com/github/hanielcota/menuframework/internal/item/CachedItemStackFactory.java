package com.github.hanielcota.menuframework.internal.item;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import com.github.hanielcota.menuframework.core.cache.MenuCacheFactory;
import com.github.hanielcota.menuframework.core.profile.PlayerProfileService;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

public final class CachedItemStackFactory implements ItemStackFactory {

  private static final Logger log =
      Logger.getLogger(CachedItemStackFactory.class.getName());

  @NonNull private final Cache<ItemTemplate, ItemStack> baseCache;
  @NonNull private final PlayerProfileService playerProfileService;

  public CachedItemStackFactory(
      @NonNull MenuFrameworkConfig configuration,
      @NonNull PlayerProfileService playerProfileService) {
    this.baseCache = MenuCacheFactory.createItemStackCache(configuration);
    this.playerProfileService = playerProfileService;
  }

  private static void applyDisplayName(@NonNull ItemMeta meta, @NonNull ItemTemplate template) {
    meta.displayName(template.displayName());
  }

  private static void applyLore(@NonNull ItemMeta meta, @NonNull ItemTemplate template) {
    if (!template.lore().isEmpty()) {
      meta.lore(template.lore());
    }
  }

  private static void applyFlags(@NonNull ItemMeta meta, @NonNull ItemTemplate template) {
    meta.addItemFlags(template.flags());
  }

  private static void applyGlow(@NonNull ItemMeta meta, @NonNull ItemTemplate template) {
    if (template.glow()) {
      meta.setEnchantmentGlintOverride(true);
    }
  }

  private static void applyCustomModelData(@NonNull ItemMeta meta, @NonNull ItemTemplate template) {
    if (template.customModelData() != 0) {
      meta.setCustomModelData(template.customModelData());
    }
  }

  private static void applyLeatherMeta(
      @NonNull LeatherArmorMeta meta, @NonNull ItemTemplate template) {
    if (template.leatherColor() != null) {
      meta.setColor(template.leatherColor());
    }
  }

  private static void applyPdc(@NonNull ItemMeta meta, @NonNull ItemTemplate template) {
    var pdc = meta.getPersistentDataContainer();
    for (var entry : template.pdcData().entrySet()) {
      var key = entry.getKey();
      switch (entry.getValue()) {
        case Integer i -> pdc.set(key, PersistentDataType.INTEGER, i);
        case Double d -> pdc.set(key, PersistentDataType.DOUBLE, d);
        case Long l -> pdc.set(key, PersistentDataType.LONG, l);
        case Byte b -> pdc.set(key, PersistentDataType.BYTE, b);
        case Short s -> pdc.set(key, PersistentDataType.SHORT, s);
        case Float f -> pdc.set(key, PersistentDataType.FLOAT, f);
        case null -> {
          /* ignored */
        }
        default -> pdc.set(key, PersistentDataType.STRING, entry.getValue().toString());
      }
    }
  }

  private @NonNull ItemStack buildBase(@NonNull ItemTemplate template) {
    ItemStack item;
    try {
      item = new ItemStack(template.material(), template.amount());
    } catch (Exception e) {
      log.log(
          Level.WARNING,
          "Failed to create ItemStack for material: {0}",
          template.material());
      item = new ItemStack(org.bukkit.Material.STONE, template.amount());
    }
    ItemMeta meta = item.getItemMeta();
    if (meta == null) return item;

    applyDisplayName(meta, template);
    applyLore(meta, template);
    applyFlags(meta, template);
    applyGlow(meta, template);
    applyCustomModelData(meta, template);
    applySpecialMeta(meta, template);
    applyPdc(meta, template);

    if (!item.setItemMeta(meta)) {
      log.log(
          Level.WARNING,
          "Failed to apply ItemMeta for material: {0}. Some properties may be unsupported.",
          template.material());
    }
    return item;
  }

  private void applySpecialMeta(@NonNull ItemMeta meta, @NonNull ItemTemplate template) {
    if (meta instanceof SkullMeta skullMeta) {
      applySkullMeta(skullMeta, template);
      return;
    }
    if (meta instanceof LeatherArmorMeta leatherMeta) {
      applyLeatherMeta(leatherMeta, template);
    }
  }

  private void applySkullMeta(@NonNull SkullMeta meta, @NonNull ItemTemplate template) {
    if (template.headUuid() != null) {
      playerProfileService.applyPlayerUuid(meta, template.headUuid());
      return;
    }
    if (template.headTexture() != null) {
      playerProfileService.applyBase64Texture(meta, template.headTexture());
    }
  }

  @Override
  public @NonNull ItemStack create(@NonNull ItemTemplate template) {
    ItemStack base = baseCache.get(template, this::buildBase);
    return base.clone();
  }

  @Override
  public void clearCache() {
    baseCache.invalidateAll();
  }
}
