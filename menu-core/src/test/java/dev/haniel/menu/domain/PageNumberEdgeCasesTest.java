package dev.haniel.menu.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/** Adversarial boundary and overflow probes for {@link PageNumber}. */
class PageNumberEdgeCasesTest {

  @Test
  void zeroIsAccepted() {
    assertEquals(0, new PageNumber(0).value());
  }

  @Test
  void previousOnFirstReturnsSameInstance() {
    // Javadoc: "clamped at the first page"; first().previous() must be page zero.
    PageNumber first = PageNumber.first();
    assertSame(first, first.previous());
  }

  @Test
  void nextThenPreviousRoundTrips() {
    assertEquals(0, PageNumber.first().next().previous().value());
  }

  @Test
  void nextAtIntMaxOverflowsToInvalidPage() {
    // value + 1 at Integer.MAX_VALUE wraps to Integer.MIN_VALUE, which the
    // constructor rejects. next() is documented as "the page after this one"
    // with no overflow caveat; advancing should not blow up with IAE.
    PageNumber last = new PageNumber(Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, last.next().value());
  }
}
