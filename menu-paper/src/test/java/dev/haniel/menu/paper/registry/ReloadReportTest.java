package dev.haniel.menu.paper.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.domain.MenuId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReloadReportTest {

  @Test
  void emptyReportIsSuccessfulWithNoCount() {
    ReloadReport report = ReloadReport.empty();

    assertTrue(report.successful());
    assertEquals(0, report.successCount());
  }

  @Test
  void countsReloadedMenus() {
    ReloadReport report = new ReloadReport(List.of(new MenuId("a"), new MenuId("b")), List.of());

    assertTrue(report.successful());
    assertEquals(2, report.successCount());
  }

  @Test
  void anyFailureMakesItUnsuccessful() {
    ReloadReport report =
        new ReloadReport(
            List.of(new MenuId("a")), List.of(new ReloadFailure(new MenuId("b"), "boom")));

    assertFalse(report.successful());
    assertEquals(1, report.successCount());
  }

  @Test
  void copiesListsDefensively() {
    List<MenuId> reloaded = new ArrayList<>(List.of(new MenuId("a")));
    ReloadReport report = new ReloadReport(reloaded, List.of());

    reloaded.clear();

    assertEquals(1, report.successCount());
  }
}
