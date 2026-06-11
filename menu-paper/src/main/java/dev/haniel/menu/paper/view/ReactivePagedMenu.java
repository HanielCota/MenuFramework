package dev.haniel.menu.paper.view;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.binding.ArgField;
import dev.haniel.menu.compiler.binding.BoundTick;
import dev.haniel.menu.compiler.model.CompiledPagedMenu;
import dev.haniel.menu.domain.PageNumber;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.paper.hook.HookDefinitions;
import dev.haniel.menu.paper.hook.MenuHooks;
import dev.haniel.menu.paper.placeholder.ResolvedIconFactory;
import dev.haniel.menu.paper.refresh.RefreshEvents;
import dev.haniel.menu.paper.render.PageRenderer;
import dev.haniel.menu.paper.render.cache.DataVersion;
import dev.haniel.menu.paper.render.cache.PageCache;
import dev.haniel.menu.paper.render.model.Overlay;
import dev.haniel.menu.paper.render.model.PageScene;
import dev.haniel.menu.state.State;
import dev.haniel.menu.state.StateBinding;
import dev.haniel.menu.template.IconFactory;
import dev.haniel.menu.template.PagedAppearance;
import dev.haniel.menu.template.PagedContent;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
    this.plan = Objects.requireNonNull(plan, "plan");
    this.runtime = Objects.requireNonNull(runtime, "runtime");
  }

  @Override
  public void open(Player player, Object argument) {
    Object instance = plan.wiring().instantiator().create();
    PlayerId viewer = new PlayerId(player.getUniqueId());
    injectViewer(instance, viewer);
    injectArgs(instance, argument);
    MenuHooks hooks = HookDefinitions.of(instance.getClass()).bind(instance);
    AtomicReference<Runnable> unsubscribe = new AtomicReference<>(() -> {});
    PageRenderer renderer =
        new PageRenderer(
            scene(instance, viewer),
            new PageCache(runtime.logger()),
            new DataVersion(),
            runtime.inventories());
    ReactivePagedView view =
        new ReactivePagedView(
            renderer,
            states(instance),
            ticks(instance),
            closeHook(hooks, player.getUniqueId(), unsubscribe),
            runtime.scheduler().forPlayer(viewer),
            runtime.logger());
    try {
      view.show(PageNumber.first());
      player.openInventory(view.getInventory());
      view.bind();
      unsubscribe.set(subscribeRefresh(instance, view));
      hooks.fireOpen(player);
    } catch (RuntimeException exception) {
      view.close();
      throw exception;
    }
  }

  private void injectViewer(Object instance, PlayerId viewer) {
    plan.wiring().viewers().forEach(field -> field.inject(instance, viewer));
  }

  private Runnable subscribeRefresh(Object instance, ReactivePagedView view) {
    Set<Class<? extends Event>> events = RefreshEvents.of(instance.getClass());
    if (events.isEmpty()) {
      return () -> {};
    }
    return runtime.refreshSubscriber().subscribe(events, view::refresh);
  }

  private void injectArgs(Object instance, Object argument) {
    if (argument == null) {
      return;
    }
    List<ArgField> matching =
        plan.wiring().args().stream().filter(field -> field.accepts(argument)).toList();
    if (matching.isEmpty()) {
      throw new InvalidMenuException(
          "Menu '"
              + plan.appearance().id().value()
              + "' was opened with an argument of type "
              + argument.getClass().getName()
              + " but declares no matching @Arg field");
    }
    matching.forEach(field -> field.inject(instance, argument));
  }

  private Runnable closeHook(MenuHooks hooks, UUID viewer, AtomicReference<Runnable> unsubscribe) {
    // Cancel the @RefreshOn subscription first (anti-leak), then fire @OnClose. Pass the viewer
    // even
    // when offline (a close fired by the quit backstop): no-arg @OnClose handlers still run their
    // cleanup, while Player-accepting ones are skipped by MenuHooks.
    return () -> {
      unsubscribe.get().run();
      hooks.fireClose(org.bukkit.Bukkit.getPlayer(viewer));
    };
  }

  private PageScene scene(Object instance, PlayerId viewer) {
    PagedAppearance<ItemStack> appearance = plan.appearance();
    String title = runtime.placeholders().resolve(viewer, appearance.title());
    return new PageScene(
        appearance.id(),
        runtime.miniMessage().deserialize(title),
        appearance.size(),
        appearance.layout(),
        appearance.decor(),
        content(instance, viewer),
        overlay(instance));
  }

  private PagedContent<ItemStack> content(Object instance, PlayerId viewer) {
    IconFactory<ItemStack> icons =
        new ResolvedIconFactory(runtime.icons(), runtime.placeholders(), viewer);
    return new PagedContent<>(plan.wiring().provider().bind(instance), icons);
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

  private List<BoundTick> ticks(Object instance) {
    return plan.wiring().ticks().stream().map(tick -> tick.bind(instance)).toList();
  }
}
