package com.github.hanielcota.menuframework.definition;

import com.github.hanielcota.menuframework.core.text.MiniMessageProvider;
import java.util.Arrays;
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
    @Nullable Sound clickSound,
    boolean italic) {

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
    pdcData = Map.copyOf(pdcData);
  }

  public static Builder builder(@NonNull Material material) {
    return new Builder(Objects.requireNonNull(material, "material"));
  }

  /**
   * Creates a simple item template with just a material and display name.
   *
   * <p>Example: {@code ItemTemplate.of(Material.DIAMOND, "<aqua>Diamond")}
   *
   * @param material the item material
   * @param name the display name as a MiniMessage string
   * @return a new item template
   */
  public static @NonNull ItemTemplate of(@NonNull Material material, @NonNull String name) {
    return builder(material).name(name).build();
  }

  /**
   * Creates an item template with material, name, and lore lines.
   *
   * <p>Example: {@code ItemTemplate.of(Material.DIAMOND, "<aqua>Diamond", "<gray>Click to buy")}
   *
   * @param material the item material
   * @param name the display name as a MiniMessage string
   * @param lore lore lines as MiniMessage strings
   * @return a new item template
   */
  public static @NonNull ItemTemplate of(
      @NonNull Material material, @NonNull String name, @NonNull String... lore) {
    var builder = builder(material).name(name);
    if (lore.length > 0) {
      builder.lore(lore);
    }
    return builder.build();
  }

  /**
   * Creates a glowing item template.
   *
   * <p>Example: {@code ItemTemplate.glowing(Material.NETHER_STAR, "<rainbow>Special Item")}
   *
   * @param material the item material
   * @param name the display name as a MiniMessage string
   * @return a new glowing item template
   */
  public static @NonNull ItemTemplate glowing(@NonNull Material material, @NonNull String name) {
    return builder(material).name(name).glow(true).build();
  }

  /**
   * Creates a player head item template.
   *
   * <p>Example: {@code ItemTemplate.head(player.getUniqueId(), "<yellow>Profile")}
   *
   * @param uuid the player's UUID
   * @param name the display name as a MiniMessage string
   * @return a new player head item template
   */
  public static @NonNull ItemTemplate head(@NonNull UUID uuid, @NonNull String name) {
    return builder(Material.PLAYER_HEAD).name(name).head(uuid).build();
  }

  /**
   * Creates a player head item template with a base64 texture.
   *
   * <p>Example: {@code ItemTemplate.head(textureBase64, "<yellow>Custom Head")}
   *
   * @param base64Texture the base64 encoded texture
   * @param name the display name as a MiniMessage string
   * @return a new player head item template
   */
  public static @NonNull ItemTemplate head(@NonNull String base64Texture, @NonNull String name) {
    return builder(Material.PLAYER_HEAD).name(name).head(base64Texture).build();
  }

  @Override
  public @NonNull ItemFlag[] flags() {
    return flags.clone();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ItemTemplate other)) return false;
    return glow == other.glow
        && amount == other.amount
        && customModelData == other.customModelData
        && italic == other.italic
        && material == other.material
        && Objects.equals(displayName, other.displayName)
        && Objects.equals(lore, other.lore)
        && Arrays.equals(flags, other.flags)
        && Objects.equals(pdcData, other.pdcData)
        && Objects.equals(headTexture, other.headTexture)
        && Objects.equals(headUuid, other.headUuid)
        && Objects.equals(leatherColor, other.leatherColor)
        && clickSound == other.clickSound;
  }

  @Override
  public int hashCode() {
    int result =
        Objects.hash(
            material,
            displayName,
            lore,
            pdcData,
            glow,
            amount,
            customModelData,
            headTexture,
            headUuid,
            leatherColor,
            clickSound,
            italic);
    result = 31 * result + Arrays.hashCode(flags);
    return result;
  }

  @Override
  public @NonNull String toString() {
    return "ItemTemplate["
        + "material="
        + material
        + ", displayName="
        + displayName
        + ", lore="
        + lore
        + ", flags="
        + Arrays.toString(flags)
        + ", pdcData="
        + pdcData
        + ", glow="
        + glow
        + ", amount="
        + amount
        + ", customModelData="
        + customModelData
        + ", headTexture="
        + headTexture
        + ", headUuid="
        + headUuid
        + ", leatherColor="
        + leatherColor
        + ", clickSound="
        + clickSound
        + ", italic="
        + italic
        + ']';
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
    private boolean italic = true;

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

    public Builder lore(@NonNull String... miniMessageLines) {
      this.lore = Arrays.stream(miniMessageLines)
          .map(MiniMessageProvider::deserialize)
          .toList();
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

    public Builder glow() {
      this.glow = true;
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

    public Builder italic(boolean italic) {
      this.italic = italic;
      return this;
    }

    public Builder italic() {
      this.italic = true;
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
          clickSound,
          italic);
    }
  }
}
