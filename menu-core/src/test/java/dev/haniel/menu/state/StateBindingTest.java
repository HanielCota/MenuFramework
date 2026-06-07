package dev.haniel.menu.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class StateBindingTest {

  @Test
  void bindRegistersTheListenerOnEveryState() {
    State<String> first = State.of("a");
    State<String> second = State.of("x");
    StateBinding binding = new StateBinding(List.of(first, second));
    int[] notifications = {0};

    binding.bind(() -> notifications[0]++);
    first.set("b");
    second.set("y");

    assertEquals(2, notifications[0]);
  }

  @Test
  void unbindRemovesTheListenerFromEveryState() {
    State<String> first = State.of("a");
    State<String> second = State.of("x");
    StateBinding binding = new StateBinding(List.of(first, second));
    int[] notifications = {0};
    binding.bind(() -> notifications[0]++);

    binding.unbind();
    first.set("b");
    second.set("y");

    assertEquals(0, notifications[0]);
  }

  @Test
  void copiesTheStatesDefensively() {
    State<String> only = State.of("a");
    List<State<?>> states = new java.util.ArrayList<>(List.of(only));
    StateBinding binding = new StateBinding(states);
    int[] notifications = {0};
    binding.bind(() -> notifications[0]++);

    states.clear();
    only.set("b");

    assertEquals(1, notifications[0]);
  }

  @Test
  void bindingNoStatesNotifiesNothing() {
    StateBinding binding = new StateBinding(List.of());
    int[] notifications = {0};

    binding.bind(() -> notifications[0]++);
    binding.unbind();

    assertEquals(0, notifications[0]);
  }
}
