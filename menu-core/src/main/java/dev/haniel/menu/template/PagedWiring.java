package dev.haniel.menu.template;

import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.binding.StateField;
import dev.haniel.menu.compiler.binding.UnboundAction;
import dev.haniel.menu.compiler.binding.UnboundProvider;
import java.util.List;
import java.util.Map;

/**
 * The instance-free wiring of a paginated menu, bound to a fresh instance per open.
 *
 * @param instantiator builds the per-player instance
 * @param provider the unbound paginated content handle
 * @param overlayActions the unbound static button actions, by slot
 * @param states the {@code @Reactive} field getters
 */
public record PagedWiring(
    Instantiator instantiator,
    UnboundProvider provider,
    Map<Integer, UnboundAction> overlayActions,
    List<StateField> states) {

  public PagedWiring {
    overlayActions = Map.copyOf(overlayActions);
    states = List.copyOf(states);
  }
}
