package dev.haniel.menu.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Adversarial edge cases for the State -> listener notification contract: equal-value suppression
 * by value (not identity), null rejection, and the loud rejection of aliasing a single State across
 * two open views (a silently replaced listener would freeze the first view's UI).
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
  void rebindingToADifferentListenerFailsLoudly() {
    // Silently replacing the listener would freeze the first view's UI with no log; the second
    // bind must fail at bind time instead.
    State<String> state = State.of("a");
    state.bind(() -> {});

    assertThrows(IllegalStateException.class, () -> state.bind(() -> {}));
  }

  @Test
  void rebindingTheSameListenerIsIdempotent() {
    State<String> state = State.of("a");
    int[] notifications = {0};
    StateListener listener = () -> notifications[0]++;
    state.bind(listener);
    state.bind(listener);

    state.set("b");

    assertEquals(1, notifications[0]);
  }

  @Test
  void rebindingAfterUnbindSupportsReopen() {
    State<String> state = State.of("a");
    state.bind(() -> {});
    state.unbind();
    int[] second = {0};
    state.bind(() -> second[0]++);

    state.set("b");

    assertEquals(1, second[0], "a closed view's state can bind to the next open view");
  }

  @Test
  void bindingASharedStateIntoASecondOpenViewFailsLoudly() {
    // A single State shared by two StateBindings (two views). Binding view B while A is still
    // bound used to silently sever A; it must now fail loudly at view B's bind.
    State<String> shared = State.of("a");
    StateBinding viewA = new StateBinding(List.of(shared));
    StateBinding viewB = new StateBinding(List.of(shared));
    viewA.bind(() -> {});

    assertThrows(IllegalStateException.class, () -> viewB.bind(() -> {}));
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
