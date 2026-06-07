package dev.haniel.menu.config;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.Slot;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.CRC32;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * Loads a menu's appearance from a YAML file, once, at boot or reload.
 *
 * <p>This is the only place that touches configuration IO. Files are resolved as {@code
 * <directory>/<menuId>.yml}. A malformed or missing file fails with a message aimed at the server
 * owner rather than a raw stack trace.
 */
public final class MenuLoader {

  private final Path directory;
  private final ConcurrentMap<MenuId, CachedConfig> cache = new ConcurrentHashMap<>();

  /**
   * Creates a loader rooted at the given menus directory.
   *
   * @param directory the directory holding the {@code <menuId>.yml} files; never null
   */
  public MenuLoader(Path directory) {
    this.directory = directory;
  }

  /**
   * Loads the configuration for the given menu id.
   *
   * @param id the menu id; never null
   * @return the parsed, validated configuration
   * @throws InvalidMenuException if the file is missing, empty or malformed
   */
  public MenuConfig load(MenuId id) {
    return read(id, resolveWithin(id));
  }

  private Path resolveWithin(MenuId id) {
    Path base = directory.toAbsolutePath().normalize();
    Path file = base.resolve(id.value() + ".yml").normalize();
    if (!file.startsWith(base)) {
      throw new InvalidMenuException(
          "Menu '" + id.value() + "' resolves outside the menus directory");
    }
    return file;
  }

  private MenuConfig read(MenuId id, Path file) {
    try {
      FileStamp stamp = stamp(file);
      CachedConfig cached = cache.get(id);
      if (cached != null && cached.matches(stamp)) {
        return cached.config();
      }
      MenuConfig config = require(id, file, parse(id, file));
      cache.put(id, new CachedConfig(stamp, config));
      return config;
    } catch (IOException exception) {
      throw new InvalidMenuException(
          "Failed to load menu '" + id.value() + "' from " + file, exception);
    }
  }

  private FileStamp stamp(Path file) throws IOException {
    BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
    return new FileStamp(attributes.lastModifiedTime(), attributes.size(), checksum(file));
  }

  private long checksum(Path file) throws IOException {
    CRC32 crc = new CRC32();
    crc.update(Files.readAllBytes(file));
    return crc.getValue();
  }

  private MenuConfig parse(MenuId id, Path file) throws IOException {
    ConfigurationNode root = YamlConfigurationLoader.builder().path(file).build().load();
    requireButtonsMap(id, root.node("buttons"));
    return root.get(MenuConfig.class);
  }

  private void requireButtonsMap(MenuId id, ConfigurationNode buttons) {
    if (!buttons.empty() && !buttons.isMap()) {
      throw new InvalidMenuException(
          "Menu '"
              + id.value()
              + "' has a malformed 'buttons' section; it must be a map of id to"
              + " button");
    }
  }

  private MenuConfig require(MenuId id, Path file, MenuConfig config) {
    if (config == null) {
      throw new InvalidMenuException("Menu '" + id.value() + "' has no configuration at " + file);
    }
    validate(id, config);
    return config;
  }

  private void validate(MenuId id, MenuConfig config) {
    validateButtons(id, config);
    config.paginationConfig().ifPresent(pagination -> validatePagination(id, config, pagination));
  }

  private void validateButtons(MenuId id, MenuConfig config) {
    config
        .buttons()
        .forEach((buttonId, button) -> validateSlot(id, buttonId, button.slot(), config.rows()));
  }

  private void validatePagination(MenuId id, MenuConfig config, PaginationConfig pagination) {
    try {
      MaskLayout.resolve(pagination.mask(), config.rows());
    } catch (RuntimeException exception) {
      throw new InvalidMenuException(
          "Menu '" + id.value() + "' has invalid pagination mask: " + exception.getMessage(),
          exception);
    }
  }

  private void validateSlot(MenuId id, String buttonId, int slot, int rows) {
    try {
      Slot.of(slot, rows);
    } catch (IllegalArgumentException exception) {
      throw new InvalidMenuException(
          "Menu '"
              + id.value()
              + "' button '"
              + buttonId
              + "' has invalid slot: "
              + exception.getMessage(),
          exception);
    }
  }

  private record CachedConfig(FileStamp stamp, MenuConfig config) {

    boolean matches(FileStamp other) {
      return stamp.equals(other);
    }
  }

  private record FileStamp(FileTime modified, long size, long checksum) {}
}
