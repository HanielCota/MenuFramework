package dev.haniel.menu.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StateTest {

  @Test
  void notifiesListenerOnlyWhenValueChanges() {
    State<String> state = State.of("a");
    int[] notifications = {0};
    state.bind(() -> notifications[0]++);

    state.set("b");
    state.set("b");
    state.set("c");

    assertEquals(2, notifications[0]);
  }

  @Test
  void storesValueBeforeBinding() {
    State<String> state = State.of("a");
    state.set("b");
    assertEquals("b", state.get());
  }

  @Test
  void unbindStopsNotifications() {
    State<String> state = State.of("a");
    int[] notifications = {0};
    state.bind(() -> notifications[0]++);
    state.unbind();

    state.set("b");

    assertEquals(0, notifications[0]);
  }
}
