package dev.haniel.menu.domain;

import dev.haniel.menu.compiler.InvalidMenuException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * The resolved layout of a pagination mask.
 *
 * <p>A value object built once at boot from the mask strings: {@code X} marks paginated content,
 * {@code #} a static border, {@code <}/{@code >} the navigation controls and a space an empty slot.
 * Resolving validates the grid (width 9, height equal to rows, at least one content slot and at
 * most one of each control) and fails the boot with a clear message otherwise.
 *
 * @param contentSlots the slots that hold paginated content, in reading order
 * @param borderSlots the slots that hold the static border
 * @param previousSlot the previous-page slot, or {@code -1} if absent
 * @param nextSlot the next-page slot, or {@code -1} if absent
 * @param size the total number of slots ({@code rows * 9})
 */
public record MaskLayout(
    int[] contentSlots, int[] borderSlots, int previousSlot, int nextSlot, int size) {

  private static final int WIDTH = 9;

  /** Stores defensive copies of the slot arrays so the value object stays immutable. */
  public MaskLayout {
    contentSlots = contentSlots.clone();
    borderSlots = borderSlots.clone();
  }

  /**
   * Returns the content slots in reading order.
   *
   * @return a copy of the content slots; safe to mutate
   */
  @Override
  public int[] contentSlots() {
    return contentSlots.clone();
  }

  /**
   * Returns how many content slots the mask has, without copying the slot array.
   *
   * @return the page capacity (number of {@code X} slots)
   */
  public int contentSlotCount() {
    return contentSlots.length;
  }

  /**
   * Returns the static border slots.
   *
   * @return a copy of the border slots; safe to mutate
   */
  @Override
  public int[] borderSlots() {
    return borderSlots.clone();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof MaskLayout layout)) {
      return false;
    }
    return previousSlot == layout.previousSlot
        && nextSlot == layout.nextSlot
        && size == layout.size
        && Arrays.equals(contentSlots, layout.contentSlots)
        && Arrays.equals(borderSlots, layout.borderSlots);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(previousSlot, nextSlot, size);
    result = 31 * result + Arrays.hashCode(contentSlots);
    return 31 * result + Arrays.hashCode(borderSlots);
  }

  /**
   * Resolves the mask for a menu of the given number of rows.
   *
   * @param mask one string per row
   * @param rows the number of rows the menu declares
   * @return the resolved layout
   * @throws InvalidMenuException if the mask is the wrong shape or has no content slot
   */
  public static MaskLayout resolve(List<String> mask, int rows) {
    validateShape(mask, rows);
    int size = rows * WIDTH;
    int[] content = slotsMatching(mask, size, 'X');
    ensureHasContent(content);
    return new MaskLayout(
        content,
        slotsMatching(mask, size, '#'),
        singleSlot(mask, size, '<'),
        singleSlot(mask, size, '>'),
        size);
  }

  private static void validateShape(List<String> mask, int rows) {
    if (mask.size() != rows) {
      throw new InvalidMenuException("mask has " + mask.size() + " rows but menu declares " + rows);
    }
    mask.forEach(MaskLayout::validateWidth);
  }

  private static void validateWidth(String line) {
    if (line.length() != WIDTH) {
      throw new InvalidMenuException(
          "mask line must be 9 wide but was " + line.length() + ": " + line);
    }
    line.chars().mapToObj(value -> (char) value).forEach(MaskLayout::validateRole);
  }

  private static void validateRole(char role) {
    if (role != 'X' && role != '#' && role != '<' && role != '>' && role != ' ') {
      throw new InvalidMenuException("mask contains invalid character '" + role + "'");
    }
  }

  private static void ensureHasContent(int[] content) {
    if (content.length == 0) {
      throw new InvalidMenuException("mask has no content slot ('X')");
    }
  }

  private static int[] slotsMatching(List<String> mask, int size, char role) {
    return IntStream.range(0, size).filter(slot -> charAt(mask, slot) == role).toArray();
  }

  private static int singleSlot(List<String> mask, int size, char role) {
    int[] found = slotsMatching(mask, size, role);
    if (found.length > 1) {
      throw new InvalidMenuException("mask has more than one '" + role + "'");
    }
    return found.length == 1 ? found[0] : -1;
  }

  private static char charAt(List<String> mask, int slot) {
    return mask.get(slot / WIDTH).charAt(slot % WIDTH);
  }
}
