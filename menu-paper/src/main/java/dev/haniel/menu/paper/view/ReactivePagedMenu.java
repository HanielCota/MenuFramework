package dev.haniel.menu.paper.view;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.compiler.model.CompiledPagedMenu;
import dev.haniel.menu.domain.PageNumber;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.paper.render.PageRenderer;
import dev.haniel.menu.paper.render.cache.DataVersion;
import dev.haniel.menu.paper.render.cache.PageCache;
import dev.haniel.menu.paper.render.model.Overlay;
import dev.haniel.menu.paper.render.model.PageScene;
import dev.haniel.menu.scheduler.PlayerScheduler;
import dev.haniel.menu.state.State;
import dev.haniel.menu.state.StateBinding;
import dev.haniel.menu.template.PagedAppearance;
import dev.haniel.menu.template.PagedContent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A reactive paginated menu, shared across players.
 *
 * <p>Each open creates a fresh per-player instance, binds its provider, buttons and states, and
 * builds an independent {@link ReactivePagedView} with its own page cache.
 */
public final class ReactivePagedMenu implements PaperMenu {

  private final CompiledPagedMenu<ItemStack> plan;
  private final MenuRuntime runtime;

  /**
   * Wraps a compiled paginated plan with the platform runtime.
   *
   * @param plan the shared compiled plan; never null
   * @param runtime the platform services; never null
   */
  public ReactivePagedMenu(CompiledPagedMenu<ItemStack> plan, MenuRuntime runtime) {
    this.plan = plan;
    this.runtime = runtime;
  }

  @Override
  public void open(Player player) {
    Object instance = plan.wiring().instantiator().create();
    PageRenderer renderer =
        new PageRenderer(
            scene(instance), new PageCache(runtime.logger()), new DataVersion(), runtime.inventories());
    ReactivePagedView view =
        new ReactivePagedView(renderer, states(instance), scheduler(player), runtime.logger());
    try {
      view.show(PageNumber.first());
      player.openInventory(view.getInventory());
      view.bind();
    } catch (RuntimeException exception) {
      view.close();
      throw exception;
    }
  }

  private PlayerScheduler scheduler(Player player) {
    return runtime.scheduler().forPlayer(new PlayerId(player.getUniqueId()));
  }

  private PageScene scene(Object instance) {
    PagedAppearance<ItemStack> appearance = plan.appearance();
    return new PageScene(
        appearance.id(),
        runtime.miniMessage().deserialize(appearance.title()),
        appearance.size(),
        appearance.layout(),
        appearance.decor(),
        content(instance),
        overlay(instance));
  }

  private PagedContent<ItemStack> content(Object instance) {
    return new PagedContent<>(plan.wiring().provider().bind(instance), runtime.icons());
  }

  private Overlay overlay(Object instance) {
    return new Overlay(plan.appearance().overlayVisuals(), boundActions(instance));
  }

  private Map<Integer, MenuAction> boundActions(Object instance) {
    return plan.wiring().overlayActions().entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().bind(instance)));
  }

  private StateBinding states(Object instance) {
    List<State<?>> read =
        plan.wiring().states().stream().<State<?>>map(field -> field.read(instance)).toList();
    return new StateBinding(read);
  }
}
