package com.hanielfialho.menuframework.api;

/**
 * Logical reason why a menu session terminated.
 *
 * <p>This enum decouples the public API from version-specific Paper close reasons. Platform values
 * unknown to the framework are mapped to {@link #UNKNOWN}.
 */
public enum MenuCloseReason {

  /** The player closed the inventory view normally, for example with ESC. */
  PLAYER,

  /** A menu button called {@link MenuInteraction#close()}. */
  BUTTON,

  /** The session was replaced through {@link MenuInteraction#open(Menu, Object)}. */
  NAVIGATION,

  /** A previous history entry was restored through {@link MenuInteraction#back()}. */
  BACK,

  /** An external root open or another inventory replaced the session. */
  REPLACED,

  /** The player disconnected. */
  QUIT,

  /** The platform closed the view because the player died. */
  DEATH,

  /** The platform closed the view because the player teleported. */
  TELEPORT,

  /** The player could no longer use the inventory. */
  CANT_USE,

  /** The associated inventory was unloaded. */
  UNLOADED,

  /** The framework API or another plugin requested the close. */
  PLUGIN,

  /** A replacement failed after beginning an open transition. */
  OPEN_FAILED,

  /** The platform reported an unrecognized close reason. */
  UNKNOWN
}
