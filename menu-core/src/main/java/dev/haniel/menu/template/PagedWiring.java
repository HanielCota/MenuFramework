package dev.haniel.menu.template;

import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.binding.StateField;
import dev.haniel.menu.compiler.binding.UnboundAction;
import dev.haniel.menu.compiler.binding.UnboundProvider;
import dev.haniel.menu.compiler.binding.UnboundTick;
import java.util.List;
import java.util.Map;

/**
 * The instance-free wiring of a paginated menu, bound to a fresh instance per open.
 *
 * @param instantiator builds the per-player instance
 * @param provider the unbound paginated content handle
 * @param overlayActions the unbound static button actions, by slot
 * @param states the {@code @Reactive} field getters
 * @param ticks the unbound {@code @Tick} handles
 */
public record PagedWiring(
    Instantiator instantiator,
    UnboundProvider provider,
    Map<Integer, UnboundAction> overlayActions,
    List<StateField> states,
    List<UnboundTick> ticks) {

  public PagedWiring {
    overlayActions = Map.copyOf(overlayActions);
    states = List.copyOf(states);
    ticks = List.copyOf(ticks);
  }

  /**
   * Creates wiring with no periodic ticks.
   *
   * @param instantiator builds the per-player instance
   * @param provider the unbound paginated content handle
   * @param overlayActions the unbound static button actions, by slot
   * @param states the reactive field getters
   */
  public PagedWiring(
      Instantiator instantiator,
      UnboundProvider provider,
      Map<Integer, UnboundAction> overlayActions,
      List<StateField> states) {
    this(instantiator, provider, overlayActions, states, List.of());
  }
}
