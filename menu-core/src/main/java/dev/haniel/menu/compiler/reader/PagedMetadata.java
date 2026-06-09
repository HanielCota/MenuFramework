package dev.haniel.menu.compiler.reader;

import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.binding.StateField;
import dev.haniel.menu.compiler.binding.UnboundAction;
import dev.haniel.menu.compiler.binding.UnboundProvider;
import dev.haniel.menu.compiler.binding.UnboundTick;
import dev.haniel.menu.compiler.binding.ViewerField;
import dev.haniel.menu.compiler.model.PagedStructure;
import dev.haniel.menu.domain.ButtonId;
import dev.haniel.menu.domain.MenuId;
import java.util.List;
import java.util.Map;

/**
 * The instance-free reflection result of a paginated menu class.
 *
 * <p>Everything {@link PagedStructureReader} discovers except how to instantiate the class; pairing
 * it with an {@link Instantiator} yields the full {@link PagedStructure}.
 */
record PagedMetadata(
    MenuId id,
    UnboundProvider provider,
    Map<ButtonId, UnboundAction> buttons,
    List<StateField> states,
    List<UnboundTick> ticks,
    List<ViewerField> viewers) {

  PagedStructure structure(Instantiator instantiator) {
    return new PagedStructure(id, instantiator, provider, buttons, states, ticks, viewers);
  }
}
