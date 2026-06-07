package dev.haniel.menu.click;

/**
 * The kind of click that triggered a button, independent of any Bukkit type.
 *
 * <p>{@link #OTHER} covers every interaction not explicitly modelled here.
 */
public enum ClickType {
  LEFT,
  RIGHT,
  SHIFT_LEFT,
  SHIFT_RIGHT,
  MIDDLE,
  OTHER
}
