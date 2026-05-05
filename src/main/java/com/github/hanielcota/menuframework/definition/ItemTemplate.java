package com.github.hanielcota.menuframework.definition;

import com.github.hanielcota.menuframework.internal.text.MiniMessageProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemFlag;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record ItemTemplate(
    @NonNull Material material,
    @NonNull Component displayName,
    @NonNull List<Component> lore,
    @NonNull ItemFlag[] flags,
    @NonNull Map<org.bukkit.NamespacedKey, Object> pdcData,
    boolean glow,
    int amount,
    int customModelData,
    @Nullable String headTexture,
    @Nullable UUID headUuid,
    @Nullable Color leatherColor,
    @Nullable Sound clickSound) {

  private static final int MIN_AMOUNT = 1;
  private static final int MAX_AMOUNT = 64;

  public ItemTemplate {
    Objects.requireNonNull(material, "material");
    Objects.requireNonNull(displayName, "displayName");
    Objects.requireNonNull(lore, "lore");
    Objects.requireNonNull(flags, "flags");
    Objects.requireNonNull(pdcData, "pdcData");

    if (amount < MIN_AMOUNT || amount > MAX_AMOUNT) {
      throw new IllegalArgumentException(
          "Amount must be between " + MIN_AMOUNT + " and " + MAX_AMOUNT + ", got: " + amount);
    }
    lore = List.copyOf(lore);
    flags = flags.clone();
    pdcData = Map.copyOf(pdcData);
  }

  public static Builder builder(@NonNull Material material) {
    return new Builder(Objects.requireNonNull(material, "material"));
  }

  @Override
  public @NonNull ItemFlag[] flags() {
    return flags.clone();
  }

  public static final class Builder {
    @NonNull private final Material material;
    private final Map<NamespacedKey, Object> pdcData = new HashMap<>();
    private Component displayName = Component.empty();
    private List<Component> lore = List.of();
    private ItemFlag[] flags = new ItemFlag[0];
    private boolean glow = false;
    private int amount = 1;
    private int customModelData = 0;
    private String headTexture;
    private UUID headUuid;
    private Color leatherColor;
    private Sound clickSound;

    private Builder(@NonNull Material material) {
      this.material = Objects.requireNonNull(material, "material");
    }

    public Builder name(@NonNull Component name) {
      this.displayName = Objects.requireNonNull(name, "name");
      return this;
    }

    public Builder name(@NonNull String miniMessage) {
      this.displayName =
          MiniMessageProvider.deserialize(Objects.requireNonNull(miniMessage, "miniMessage"));
      return this;
    }

    public Builder lore(@NonNull List<Component> lore) {
      this.lore = List.copyOf(Objects.requireNonNull(lore, "lore"));
      return this;
    }

    public Builder flags(@NonNull ItemFlag... flags) {
      this.flags = Objects.requireNonNull(flags, "flags").clone();
      return this;
    }

    public Builder glow(boolean glow) {
      this.glow = glow;
      return this;
    }

    public Builder amount(int amount) {
      if (amount < MIN_AMOUNT || amount > MAX_AMOUNT) {
        throw new IllegalArgumentException(
            "Amount must be between " + MIN_AMOUNT + " and " + MAX_AMOUNT + ", got: " + amount);
      }
      this.amount = amount;
      return this;
    }

    public Builder customModelData(int data) {
      this.customModelData = data;
      return this;
    }

    public Builder head(@NonNull UUID uuid) {
      this.headUuid = Objects.requireNonNull(uuid, "uuid");
      return this;
    }

    public Builder head(@NonNull String base64Texture) {
      this.headTexture = Objects.requireNonNull(base64Texture, "base64Texture");
      return this;
    }

    public Builder leatherColor(@NonNull Color color) {
      this.leatherColor = Objects.requireNonNull(color, "color");
      return this;
    }

    public Builder clickSound(@NonNull Sound sound) {
      this.clickSound = Objects.requireNonNull(sound, "sound");
      return this;
    }

    public Builder pdc(@NonNull NamespacedKey key, @NonNull Object value) {
      this.pdcData.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
      return this;
    }

    public ItemTemplate build() {
      return new ItemTemplate(
          material,
          displayName,
          lore,
          flags,
          pdcData,
          glow,
          amount,
          customModelData,
          headTexture,
          headUuid,
          leatherColor,
          clickSound);
    }
  }
}
