package dev.haniel.menu.paper.discovery;

import java.util.ArrayList;
import java.util.List;

/**
 * A first-class collection of per-menu boot failures, reported together at the end of a scan.
 *
 * <p>The scan does not abort on the first bad menu; it records every failure and then fails the
 * boot as a whole, so the owner sees all broken menus at once with the class and cause.
 */
public final class MenuErrors {

  private final List<String> failures = new ArrayList<>();

  /**
   * Records a failure for the given menu class.
   *
   * @param type the class that failed; never null
   * @param cause the failure; never null
   */
  public void add(Class<?> type, Throwable cause) {
    failures.add(type.getName() + " -> " + cause.getMessage());
  }

  /**
   * Fails the boot if any menu failed, listing them all.
   *
   * @throws MenuDiscoveryException if at least one failure was recorded
   */
  public void failIfAny() {
    if (failures.isEmpty()) {
      return;
    }
    throw new MenuDiscoveryException("Failed to register menus: " + String.join("; ", failures));
  }
}
