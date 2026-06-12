package dev.haniel.menu.template;

import dev.haniel.menu.compiler.binding.ArgField;
import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.binding.StateField;
import dev.haniel.menu.compiler.binding.UnboundAction;
import dev.haniel.menu.compiler.binding.UnboundContent;
import dev.haniel.menu.compiler.binding.UnboundTick;
import dev.haniel.menu.compiler.binding.ViewerField;
import java.util.List;
import java.util.Map;

/**
 * The instance-free wiring of a paginated menu, bound to a fresh instance per open.
 *
 * @param instantiator builds the per-player instance
 * @param provider the unbound paginated content source (eager list or lazy page)
 * @param overlayActions the unbound static button actions, by slot
 * @param states the {@code @Reactive} field getters
 * @param ticks the unbound {@code @Tick} handles
 * @param viewers the {@code @Viewer} field setters
 * @param args the {@code @Arg} field setters
 * @param buttonSlots the overlay button id to slot map, for per-viewer {@code @Visible} rules
 */
public record PagedWiring(
    Instantiator instantiator,
    UnboundContent provider,
    Map<Integer, UnboundAction> overlayActions,
    List<StateField> states,
    List<UnboundTick> ticks,
    List<ViewerField> viewers,
    List<ArgField> args,
    Map<String, Integer> buttonSlots) {

  public PagedWiring {
    overlayActions = Map.copyOf(overlayActions);
    states = List.copyOf(states);
    ticks = List.copyOf(ticks);
    viewers = List.copyOf(viewers);
    args = List.copyOf(args);
    buttonSlots = Map.copyOf(buttonSlots);
  }

  /**
   * Creates wiring with no periodic ticks and no viewer or argument injection.
   *
   * @param instantiator builds the per-player instance
   * @param provider the unbound paginated content source (eager list or lazy page)
   * @param overlayActions the unbound static button actions, by slot
   * @param states the reactive field getters
   */
  public PagedWiring(
      Instantiator instantiator,
      UnboundContent provider,
      Map<Integer, UnboundAction> overlayActions,
      List<StateField> states) {
    this(instantiator, provider, overlayActions, states, List.of(), List.of(), List.of());
  }

  /**
   * Creates wiring with no button-to-slot map (no {@code @Visible} rules).
   *
   * @param instantiator builds the per-player instance
   * @param provider the unbound paginated content source
   * @param overlayActions the unbound static button actions, by slot
   * @param states the reactive field getters
   * @param ticks the unbound tick handles
   * @param viewers the viewer field setters
   * @param args the argument field setters
   */
  public PagedWiring(
      Instantiator instantiator,
      UnboundContent provider,
      Map<Integer, UnboundAction> overlayActions,
      List<StateField> states,
      List<UnboundTick> ticks,
      List<ViewerField> viewers,
      List<ArgField> args) {
    this(instantiator, provider, overlayActions, states, ticks, viewers, args, Map.of());
  }
}
