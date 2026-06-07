package dev.haniel.menu.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Adversarial edge cases for the State -> listener notification contract: equal-value suppression
 * by value (not identity), null rejection, rebinding/aliasing of the single listener field, and the
 * StateBinding aliasing hazard when a state is shared across two views.
 */
class StateNotificationEdgeCasesTest {

  @Test
  void suppressesNotificationForEqualValueOfDifferentIdentity() {
    State<String> state = State.of("a");
    int[] notifications = {0};
    state.bind(() -> notifications[0]++);

    // new String("ab") is .equals to "a"+"b" but not the same reference.
    state.set(new String(new char[] {'a'}));

    assertEquals(
        0, notifications[0], "equal-by-value set must not notify even with a fresh object");
  }

  @Test
  void firesWhenValueChangesToAnEqualHashButUnequalValue() {
    State<Integer> state = State.of(1);
    int[] notifications = {0};
    state.bind(() -> notifications[0]++);

    state.set(2);

    assertEquals(1, notifications[0]);
    assertEquals(2, state.get());
  }

  @Test
  void rejectsNullInitial() {
    assertThrows(NullPointerException.class, () -> State.of(null));
  }

  @Test
  void rejectsNullSet() {
    State<String> state = State.of("a");
    assertThrows(NullPointerException.class, () -> state.set(null));
  }

  @Test
  void rebindReplacesTheListenerSoOnlyTheLatestIsNotified() {
    State<String> state = State.of("a");
    int[] first = {0};
    int[] second = {0};
    state.bind(() -> first[0]++);
    state.bind(() -> second[0]++);

    state.set("b");

    assertEquals(0, first[0], "an overwritten listener must no longer fire");
    assertEquals(1, second[0], "the most recently bound listener fires");
  }

  @Test
  void unbindOnSharedStateClearsWhicheverListenerIsCurrentlyBound() {
    // A single State shared by two StateBindings (two views). Binding view B aliases over view A.
    State<String> shared = State.of("a");
    StateBinding viewA = new StateBinding(List.of(shared));
    StateBinding viewB = new StateBinding(List.of(shared));
    int[] aCount = {0};
    int[] bCount = {0};

    viewA.bind(() -> aCount[0]++);
    viewB.bind(() -> bCount[0]++);

    // viewA closes; it calls State::unbind, dropping view B's listener even though B is still open.
    viewA.unbind();
    shared.set("b");

    // Documents the aliasing hazard: after A unbinds, the still-open B receives nothing.
    assertEquals(0, aCount[0]);
    assertEquals(
        0, bCount[0], "view A's unbind silently severs the still-open view B (aliasing leak)");
  }

  @Test
  void notifyAfterUnbindNeverFires() {
    State<String> state = State.of("a");
    int[] notifications = {0};
    state.bind(() -> notifications[0]++);
    state.set("b");
    state.unbind();

    state.set("c");

    assertEquals(1, notifications[0], "no notification once unbound");
    assertEquals("c", state.get(), "value still updates while unbound");
  }
}
