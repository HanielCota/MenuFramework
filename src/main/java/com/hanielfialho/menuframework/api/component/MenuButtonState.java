package com.hanielfialho.menuframework.api.component;

/** Visual and interaction state of a reusable menu button. */
public enum MenuButtonState {

  /** Render the enabled icon and install the click handler. */
  ENABLED,

  /** Render the disabled icon without a click handler. */
  DISABLED,

  /** Do not assign the slot, allowing the frame background to remain visible. */
  HIDDEN
}
