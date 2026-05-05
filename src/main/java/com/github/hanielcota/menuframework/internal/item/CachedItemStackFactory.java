package com.github.hanielcota.menuframework.internal.item;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.internal.cache.MenuCacheFactory;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

public final class CachedItemStackFactory implements ItemStackFactory {

  private static final String TEXTURES_PROPERTY_KEY = "textures";
  @NonNull
  private final Cache<ItemTemplate, ItemStack> baseCache;

  public CachedItemStackFactory(@NonNull MenuFrameworkConfig configuration) {
    this.baseCache = MenuCacheFactory.createItemStackCache(configuration);
  }

  private static @NonNull ItemStack buildBase(@NonNull ItemTemplate template) {
    ItemStack item = new ItemStack(template.material(), template.amount());
    ItemMeta meta = item.getItemMeta();
    if (meta == null) return item;

    applyDisplayName(meta, template);
    applyLore(meta, template);
    applyFlags(meta, template);
    applyGlow(meta, template);
    applyCustomModelData(meta, template);
    applySpecialMeta(meta, template);
    applyPdc(meta, template);

    item.setItemMeta(meta);
    return item;
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

  private static void applySpecialMeta(@NonNull ItemMeta meta, @NonNull ItemTemplate template) {
    if (meta instanceof SkullMeta skullMeta) {
      applySkullMeta(skullMeta, template);
    } else if (meta instanceof LeatherArmorMeta leatherMeta) {
      applyLeatherMeta(leatherMeta, template);
    }
  }

  private static void applySkullMeta(@NonNull SkullMeta meta, @NonNull ItemTemplate template) {
    if (template.headUuid() != null) {
      var offlinePlayer = Bukkit.getOfflinePlayer(template.headUuid());
      if (offlinePlayer != null) {
        meta.setOwningPlayer(offlinePlayer);
      }
      return;
    }
    if (template.headTexture() == null) return;
    var profile = Bukkit.createProfile(UUID.randomUUID());
    if (profile == null) return;
    profile.setProperty(new ProfileProperty(TEXTURES_PROPERTY_KEY, template.headTexture()));
    meta.setPlayerProfile(profile);
  }

  private static void applyLeatherMeta(@NonNull LeatherArmorMeta meta, @NonNull ItemTemplate template) {
    if (template.leatherColor() != null) {
      meta.setColor(template.leatherColor());
    }
  }

  private static void applyPdc(@NonNull ItemMeta meta, @NonNull ItemTemplate template) {
    var pdc = meta.getPersistentDataContainer();
    for (var entry : template.pdcData().entrySet()) {
      switch (entry.getValue()) {
        case Integer i -> pdc.set(entry.getKey(), PersistentDataType.INTEGER, i);
        case Double d -> pdc.set(entry.getKey(), PersistentDataType.DOUBLE, d);
        case Long l -> pdc.set(entry.getKey(), PersistentDataType.LONG, l);
        case Byte b -> pdc.set(entry.getKey(), PersistentDataType.BYTE, b);
        case Short s -> pdc.set(entry.getKey(), PersistentDataType.SHORT, s);
        case Float f -> pdc.set(entry.getKey(), PersistentDataType.FLOAT, f);
        case null -> {
          /* ignore */
        }
        default -> pdc.set(entry.getKey(), PersistentDataType.STRING, entry.getValue().toString());
      }
    }
  }

  @Override
  public @NonNull ItemStack create(@NonNull ItemTemplate template) {
    ItemStack base = baseCache.get(template, CachedItemStackFactory::buildBase);
    return base.clone();
  }

  @Override
  public void clearCache() {
    baseCache.invalidateAll();
  }
}
