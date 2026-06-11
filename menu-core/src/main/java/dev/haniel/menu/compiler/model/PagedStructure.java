package dev.haniel.menu.compiler.model;

import dev.haniel.menu.compiler.binding.ArgField;
import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.binding.StateField;
import dev.haniel.menu.compiler.binding.UnboundAction;
import dev.haniel.menu.compiler.binding.UnboundContent;
import dev.haniel.menu.compiler.binding.UnboundTick;
import dev.haniel.menu.compiler.binding.ViewerField;
import dev.haniel.menu.domain.ButtonId;
import dev.haniel.menu.domain.MenuId;
import java.util.List;
import java.util.Map;

/**
 * The boot-discovered structure of a paginated menu: everything instance-free.
 *
 * <p>Holds unbound handles only; values are bound to a fresh instance at open time.
 *
 * @param id the menu id from {@code @Menu}
 * @param instantiator builds a per-player instance
 * @param provider the unbound {@code @Paginated} content source (eager list or lazy page)
 * @param buttons the unbound {@code @Button} actions by logical id
 * @param states the {@code @Reactive} field getters
 * @param ticks the unbound {@code @Tick} handles
 * @param viewers the {@code @Viewer} field setters
 * @param args the {@code @Arg} field setters
 */
public record PagedStructure(
    MenuId id,
    Instantiator instantiator,
    UnboundContent provider,
    Map<ButtonId, UnboundAction> buttons,
    List<StateField> states,
    List<UnboundTick> ticks,
    List<ViewerField> viewers,
    List<ArgField> args) {

  public PagedStructure {
    buttons = Map.copyOf(buttons);
    states = List.copyOf(states);
    ticks = List.copyOf(ticks);
    viewers = List.copyOf(viewers);
    args = List.copyOf(args);
  }

  /**
   * Creates a structure with no periodic ticks and no viewer or argument injection.
   *
   * @param id the menu id
   * @param instantiator builds a per-player instance
   * @param provider the unbound paginated content source (eager list or lazy page)
   * @param buttons the unbound button actions by id
   * @param states the reactive field getters
   */
  public PagedStructure(
      MenuId id,
      Instantiator instantiator,
      UnboundContent provider,
      Map<ButtonId, UnboundAction> buttons,
      List<StateField> states) {
    this(id, instantiator, provider, buttons, states, List.of(), List.of(), List.of());
  }
}
