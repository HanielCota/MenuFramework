package dev.haniel.menu.paper.registry;

import dev.haniel.menu.domain.MenuId;
import java.util.List;

/**
 * Reports the result of a reload operation.
 *
 * @param reloaded ids that were reloaded and swapped successfully
 * @param failures menus that failed to reload
 */
public record ReloadReport(List<MenuId> reloaded, List<ReloadFailure> failures) {

  public ReloadReport {
    reloaded = List.copyOf(reloaded);
    failures = List.copyOf(failures);
  }

  /**
   * Returns an empty report.
   *
   * @return a report with no successes and no failures
   */
  public static ReloadReport empty() {
    return new ReloadReport(List.of(), List.of());
  }

  /**
   * Returns whether every attempted reload succeeded.
   *
   * @return {@code true} when there are no failures
   */
  public boolean successful() {
    return failures.isEmpty();
  }

  /**
   * Returns the number of successfully reloaded menus.
   *
   * @return the success count
   */
  public int successCount() {
    return reloaded.size();
  }
}
