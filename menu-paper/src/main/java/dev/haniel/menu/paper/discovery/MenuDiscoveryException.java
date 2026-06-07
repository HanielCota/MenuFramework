package dev.haniel.menu.paper.discovery;

/** Thrown when one or more menus fail to be discovered, instantiated or compiled at boot. */
public final class MenuDiscoveryException extends RuntimeException {

  public MenuDiscoveryException(String message) {
    super(message);
  }
}
