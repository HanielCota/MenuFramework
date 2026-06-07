package dev.haniel.menu.paper.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.domain.MenuId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Adversarial invariants for {@link ReloadReport}: defensive copies, immutability, null handling. */
class ReloadReportEdgeCasesTest {

  /** The failures list must be copied defensively, like the reloaded list. */
  @Test
  void copiesFailuresDefensively() {
    List<ReloadFailure> failures =
        new ArrayList<>(List.of(new ReloadFailure(new MenuId("a"), "boom")));
    ReloadReport report = new ReloadReport(List.of(), failures);

    failures.clear();

    assertEquals(1, report.failures().size());
  }

  /** The reloaded list returned by the accessor must be immutable. */
  @Test
  void reloadedListIsImmutable() {
    ReloadReport report = new ReloadReport(List.of(new MenuId("a")), List.of());

    assertThrows(
        UnsupportedOperationException.class, () -> report.reloaded().add(new MenuId("b")));
  }

  /** The failures list returned by the accessor must be immutable. */
  @Test
  void failuresListIsImmutable() {
    ReloadReport report = new ReloadReport(List.of(), List.of());

    assertThrows(
        UnsupportedOperationException.class,
        () -> report.failures().add(new ReloadFailure(new MenuId("b"), "x")));
  }

  /** A null reloaded list is an illegal argument, not a leaked NullPointerException internal. */
  @Test
  void nullReloadedListIsRejected() {
    assertThrows(RuntimeException.class, () -> new ReloadReport(null, List.of()));
  }

  /** A null failures list is an illegal argument. */
  @Test
  void nullFailuresListIsRejected() {
    assertThrows(RuntimeException.class, () -> new ReloadReport(List.of(), null));
  }

  /** A report with both successes and failures is not successful. */
  @Test
  void mixedReportIsNotSuccessful() {
    ReloadReport report =
        new ReloadReport(
            List.of(new MenuId("a"), new MenuId("b")),
            List.of(new ReloadFailure(new MenuId("c"), "boom")));

    assertEquals(1, report.failures().size());
    assertEquals(2, report.successCount());
    assertFalse(report.successful());
  }
}
