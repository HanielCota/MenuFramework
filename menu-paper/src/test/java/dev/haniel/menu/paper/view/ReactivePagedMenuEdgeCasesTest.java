package dev.haniel.menu.paper.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.haniel.menu.annotation.Arg;
import dev.haniel.menu.annotation.Viewer;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.binding.UnboundProvider;
import dev.haniel.menu.compiler.model.CompiledPagedMenu;
import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.paper.annotation.RefreshOn;
import dev.haniel.menu.paper.holder.OpenMenu;
import dev.haniel.menu.paper.refresh.RefreshSubscriber;
import dev.haniel.menu.paper.render.InventoryFactory;
import dev.haniel.menu.placeholder.PlaceholderResolver;
import dev.haniel.menu.scheduler.MenuScheduler;
import dev.haniel.menu.scheduler.PlayerScheduler;
import dev.haniel.menu.scheduler.ScheduledTask;
import dev.haniel.menu.template.IconFactory;
import dev.haniel.menu.template.PagedAppearance;
import dev.haniel.menu.template.PagedDecor;
import dev.haniel.menu.template.PagedWiring;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Per-player isolation and open/recovery probes for {@link ReactivePagedMenu}, driven without a
 * server: the inventory factory and player are mocked.
 *
 * <p>Each {@code open} must build a fresh instance, renderer, cache and cursor so two viewers never
 * alias state, and a render failure mid-open must close the half-built view (anti-leak) and
 * propagate, never leaving a bound view behind.
 */
class ReactivePagedMenuEdgeCasesTest {

  @Test
  void eachOpenInstantiatesAFreshPerPlayerInstance() {
    AtomicInteger instances = new AtomicInteger();
    ReactivePagedMenu menu = menu(countingInstantiator(instances), inventoryFactory());

    menu.open(player());
    menu.open(player());

    assertEquals(2, instances.get(), "every open must build its own per-player instance");
  }

  @Test
  void twoOpensGetDistinctInventoriesSoCursorsDoNotAlias() {
    RecordingFactory factory = new RecordingFactory();
    ReactivePagedMenu menu = menu(countingInstantiator(new AtomicInteger()), factory);

    Player first = player();
    Player second = player();
    menu.open(first);
    menu.open(second);

    Inventory firstInventory = openedInventory(first);
    Inventory secondInventory = openedInventory(second);
    assertNotSame(firstInventory, secondInventory, "each viewer must own an independent inventory");
    assertEquals(2, factory.created(), "each open must allocate its own inventory and cursor");
  }

  @Test
  void openSchedulesPerPlayerSoTheRightSchedulerIsResolved() {
    RecordingScheduler scheduler = new RecordingScheduler();
    ReactivePagedMenu menu =
        new ReactivePagedMenu(
            pagedPlan(countingInstantiator(new AtomicInteger())),
            runtime(scheduler, inventoryFactory()));

    Player player = player();
    menu.open(player);

    assertTrue(
        scheduler.resolved().contains(player.getUniqueId()),
        "open must resolve the scheduler for the opening player's id");
  }

  @Test
  void failedInventoryAllocationDuringOpenPropagatesAndOpensNothing() {
    InventoryFactory exploding =
        (holder, size, title) -> {
          throw new IllegalStateException("inventory boom");
        };
    ReactivePagedMenu menu = menu(countingInstantiator(new AtomicInteger()), exploding);
    Player player = player();

    assertThrows(IllegalStateException.class, () -> menu.open(player));
    // The inventory was never built, so the player must never have had a menu opened (anti-leak).
    verify(player, never()).openInventory(any(Inventory.class));
  }

  @Test
  void viewerIsInjectedBeforeTheFirstPaginatedRender() {
    AtomicReference<PlayerId> seenAtRender = new AtomicReference<>();
    ReactivePagedMenu menu = viewerAwareMenu(seenAtRender);
    Player player = player();

    menu.open(player);

    assertEquals(
        player.getUniqueId(),
        seenAtRender.get().value(),
        "the @Paginated provider must already know the viewer on the first render");
  }

  @Test
  void argumentIsInjectedBeforeTheFirstPaginatedRender() {
    AtomicReference<String> seenAtRender = new AtomicReference<>();
    ReactivePagedMenu menu = argAwareMenu(seenAtRender);

    menu.open(player(), "target-player");

    assertEquals(
        "target-player",
        seenAtRender.get(),
        "the @Paginated provider must already see the open argument on the first render");
  }

  @Test
  void openingWithAnArgumentNoFieldAcceptsFailsLoudlyAndOpensNothing() {
    ReactivePagedMenu menu = menu(countingInstantiator(new AtomicInteger()), inventoryFactory());
    Player player = player();

    assertThrows(InvalidMenuException.class, () -> menu.open(player, 42));
    verify(player, never()).openInventory(any(Inventory.class));
  }

  @Test
  void refreshEventsAreSubscribedOnOpenWithTheDeclaredEventSet() {
    RecordingRefreshSubscriber subscriber = new RecordingRefreshSubscriber();
    ReactivePagedMenu menu =
        refreshAwareMenu(subscriber, new RecordingScheduler(), new AtomicInteger());

    menu.open(player());

    assertEquals(
        Set.of(PlayerJoinEvent.class),
        subscriber.events(),
        "open must subscribe to the menu's @RefreshOn events");
  }

  @Test
  void aRefreshEventReRendersTheOpenView() {
    RecordingRefreshSubscriber subscriber = new RecordingRefreshSubscriber();
    AtomicInteger renders = new AtomicInteger();
    ReactivePagedMenu menu = refreshAwareMenu(subscriber, new ImmediateScheduler(), renders);
    menu.open(player());
    int afterOpen = renders.get();

    subscriber.fire();

    assertTrue(renders.get() > afterOpen, "a @RefreshOn event must re-run the @Paginated render");
  }

  @Test
  void closingTheViewCancelsTheRefreshSubscription() {
    RecordingRefreshSubscriber subscriber = new RecordingRefreshSubscriber();
    ReactivePagedMenu menu =
        refreshAwareMenu(subscriber, new RecordingScheduler(), new AtomicInteger());
    Player player = player();
    menu.open(player);

    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      ((ReactivePagedView) openedInventory(player).getHolder()).close();
    }

    assertTrue(
        subscriber.unsubscribed(), "closing the view must cancel its @RefreshOn subscription");
  }

  @Test
  void menusWithoutRefreshOnDoNotSubscribe() {
    RecordingRefreshSubscriber subscriber = new RecordingRefreshSubscriber();
    ReactivePagedMenu menu =
        new ReactivePagedMenu(
            pagedPlan(countingInstantiator(new AtomicInteger())),
            refreshRuntime(new RecordingScheduler(), subscriber));

    menu.open(player());

    assertNull(subscriber.events(), "a menu without @RefreshOn must not subscribe to anything");
  }

  private static ReactivePagedMenu refreshAwareMenu(
      RefreshSubscriber subscriber, MenuScheduler scheduler, AtomicInteger renders) {
    PagedWiring wiring =
        new PagedWiring(
            new Instantiator(() -> new RefreshProbe(renders)),
            refreshProvider(),
            Map.of(),
            List.of());
    CompiledPagedMenu<ItemStack> plan = new CompiledPagedMenu<>(appearance(), wiring);
    return new ReactivePagedMenu(plan, refreshRuntime(scheduler, subscriber));
  }

  private static MenuRuntime refreshRuntime(MenuScheduler scheduler, RefreshSubscriber subscriber) {
    MiniMessage miniMessage = mock(MiniMessage.class);
    when(miniMessage.deserialize(any(String.class))).thenReturn(Component.text("title"));
    IconFactory<ItemStack> icons = icon -> mock(ItemStack.class);
    return new MenuRuntime(
        Logger.getLogger("refresh-test"),
        icons,
        miniMessage,
        scheduler,
        inventoryFactory(),
        PlaceholderResolver.none(),
        subscriber);
  }

  private static UnboundProvider refreshProvider() {
    try {
      Method products = RefreshProbe.class.getDeclaredMethod("products");
      products.setAccessible(true);
      return new UnboundProvider(MethodHandles.lookup().unreflect(products));
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(error);
    }
  }

  private static ReactivePagedMenu argAwareMenu(AtomicReference<String> sink) {
    PagedWiring wiring =
        new PagedWiring(
            new Instantiator(() -> new ArgProbe(sink)),
            argProvider(),
            Map.of(),
            List.of(),
            List.of(),
            List.of(),
            argFields());
    CompiledPagedMenu<ItemStack> plan = new CompiledPagedMenu<>(appearance(), wiring);
    return new ReactivePagedMenu(plan, runtime(new RecordingScheduler(), inventoryFactory()));
  }

  private static UnboundProvider argProvider() {
    try {
      Method products = ArgProbe.class.getDeclaredMethod("products");
      products.setAccessible(true);
      return new UnboundProvider(MethodHandles.lookup().unreflect(products));
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(error);
    }
  }

  private static List<dev.haniel.menu.compiler.binding.ArgField> argFields() {
    try {
      java.lang.reflect.Field field = ArgProbe.class.getDeclaredField("target");
      field.setAccessible(true);
      return List.of(
          new dev.haniel.menu.compiler.binding.ArgField(
              "target", String.class, MethodHandles.lookup().unreflectSetter(field)));
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(error);
    }
  }

  private static ReactivePagedMenu viewerAwareMenu(AtomicReference<PlayerId> sink) {
    PagedWiring wiring =
        new PagedWiring(
            new Instantiator(() -> new ViewerProbe(sink)),
            viewerProvider(),
            Map.of(),
            List.of(),
            List.of(),
            viewerFields(),
            List.of());
    CompiledPagedMenu<ItemStack> plan = new CompiledPagedMenu<>(appearance(), wiring);
    return new ReactivePagedMenu(plan, runtime(new RecordingScheduler(), inventoryFactory()));
  }

  private static UnboundProvider viewerProvider() {
    try {
      Method products = ViewerProbe.class.getDeclaredMethod("products");
      products.setAccessible(true);
      return new UnboundProvider(MethodHandles.lookup().unreflect(products));
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(error);
    }
  }

  private static List<dev.haniel.menu.compiler.binding.ViewerField> viewerFields() {
    try {
      java.lang.reflect.Field field = ViewerProbe.class.getDeclaredField("viewer");
      field.setAccessible(true);
      return List.of(
          new dev.haniel.menu.compiler.binding.ViewerField(
              "viewer", MethodHandles.lookup().unreflectSetter(field)));
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(error);
    }
  }

  @Test
  void openedViewIsAnOpenMenuNamedByItsId() {
    ReactivePagedMenu menu = menu(countingInstantiator(new AtomicInteger()), inventoryFactory());
    Player player = player();

    menu.open(player);

    org.bukkit.inventory.InventoryHolder holder = openedInventory(player).getHolder();
    assertTrue(
        holder instanceof OpenMenu, "the open paged view must be discoverable as an OpenMenu");
    assertEquals("paged", ((OpenMenu) holder).menuId().value());
  }

  private static Inventory openedInventory(Player player) {
    org.mockito.ArgumentCaptor<Inventory> captor =
        org.mockito.ArgumentCaptor.forClass(Inventory.class);
    verify(player).openInventory(captor.capture());
    return captor.getValue();
  }

  private ReactivePagedMenu menu(Instantiator instantiator, InventoryFactory inventories) {
    return new ReactivePagedMenu(
        pagedPlan(instantiator), runtime(new RecordingScheduler(), inventories));
  }

  private static MenuRuntime runtime(MenuScheduler scheduler, InventoryFactory inventories) {
    MiniMessage miniMessage = mock(MiniMessage.class);
    when(miniMessage.deserialize(any(String.class))).thenReturn(Component.text("title"));
    IconFactory<ItemStack> icons = icon -> mock(ItemStack.class);
    return new MenuRuntime(
        Logger.getLogger("paged-menu-test"), icons, miniMessage, scheduler, inventories);
  }

  private static PagedAppearance<ItemStack> appearance() {
    return new PagedAppearance<>(
        new MenuId("paged"),
        "<title>",
        MaskLayout.resolve(List.of("<X>      "), 1),
        new PagedDecor<>(null, null, null),
        Map.of());
  }

  private static CompiledPagedMenu<ItemStack> pagedPlan(Instantiator instantiator) {
    PagedWiring wiring = new PagedWiring(instantiator, provider(), Map.of(), List.of());
    return new CompiledPagedMenu<>(appearance(), wiring);
  }

  private static UnboundProvider provider() {
    try {
      return new UnboundProvider(
          MethodHandles.lookup()
              .findStatic(
                  ReactivePagedMenuEdgeCasesTest.class,
                  "oneItem",
                  java.lang.invoke.MethodType.methodType(List.class, Object.class)));
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(error);
    }
  }

  @SuppressWarnings("unused") // bound reflectively as the paginated provider
  private static List<MenuItem> oneItem(Object instance) {
    return List.of(MenuItem.of(Icon.of("STONE")));
  }

  private static InventoryFactory inventoryFactory() {
    return (holder, size, title) -> inventoryOfSize(size, holder);
  }

  private static Inventory inventoryOfSize(int size, InventoryHolder holder) {
    Inventory inventory = mock(Inventory.class);
    when(inventory.getSize()).thenReturn(size);
    when(inventory.getHolder()).thenReturn(holder);
    return inventory;
  }

  private static Player player() {
    Player player = mock(Player.class);
    when(player.getUniqueId()).thenReturn(UUID.randomUUID());
    when(player.openInventory(any(Inventory.class))).thenReturn(mock(InventoryView.class));
    return player;
  }

  /** Builds an instantiator that yields a distinct instance per open and counts the calls. */
  private static Instantiator countingInstantiator(AtomicInteger count) {
    return new Instantiator(
        () -> {
          count.incrementAndGet();
          return new Object();
        });
  }

  /** A paginated menu that captures its injected {@code @Viewer} the moment it renders. */
  private static final class ViewerProbe {
    @Viewer private PlayerId viewer;
    private final AtomicReference<PlayerId> sink;

    ViewerProbe(AtomicReference<PlayerId> sink) {
      this.sink = sink;
    }

    @SuppressWarnings("unused") // bound reflectively as the paginated provider
    private List<MenuItem> products() {
      sink.set(viewer);
      return List.of(MenuItem.of(Icon.of("STONE")));
    }
  }

  /** A paginated menu that re-renders on a {@code @RefreshOn} event, counting each render. */
  @RefreshOn(PlayerJoinEvent.class)
  private static final class RefreshProbe {
    private final AtomicInteger renders;

    RefreshProbe(AtomicInteger renders) {
      this.renders = renders;
    }

    @SuppressWarnings("unused") // bound reflectively as the paginated provider
    private List<MenuItem> products() {
      renders.incrementAndGet();
      return List.of(MenuItem.of(Icon.of("STONE")));
    }
  }

  /** Captures the subscription so a test can fire it and assert it is cancelled on close. */
  private static final class RecordingRefreshSubscriber implements RefreshSubscriber {
    private Set<Class<? extends Event>> events;
    private Runnable onFire;
    private boolean unsubscribed;

    @Override
    public Runnable subscribe(Set<Class<? extends Event>> events, Runnable onFire) {
      this.events = events;
      this.onFire = onFire;
      return () -> unsubscribed = true;
    }

    void fire() {
      onFire.run();
    }

    Set<Class<? extends Event>> events() {
      return events;
    }

    boolean unsubscribed() {
      return unsubscribed;
    }
  }

  /** A scheduler that runs scheduled tasks inline, so a refresh flush renders synchronously. */
  private static final class ImmediateScheduler implements MenuScheduler {
    @Override
    public PlayerScheduler forPlayer(PlayerId player) {
      return new PlayerScheduler() {
        @Override
        public ScheduledTask schedule(Runnable task) {
          task.run();
          return immediateTask();
        }

        @Override
        public ScheduledTask scheduleRepeating(Runnable task, long period) {
          return immediateTask();
        }
      };
    }

    @Override
    public Executor global() {
      return Runnable::run;
    }

    private static ScheduledTask immediateTask() {
      return new ScheduledTask() {
        @Override
        public void cancel() {}

        @Override
        public boolean scheduled() {
          return true;
        }
      };
    }
  }

  /** A paginated menu that captures its injected {@code @Arg} the moment it renders. */
  private static final class ArgProbe {
    @Arg private String target;
    private final AtomicReference<String> sink;

    ArgProbe(AtomicReference<String> sink) {
      this.sink = sink;
    }

    @SuppressWarnings("unused") // bound reflectively as the paginated provider
    private List<MenuItem> products() {
      sink.set(target);
      return List.of(MenuItem.of(Icon.of("STONE")));
    }
  }

  /** Records every inventory it creates so a test can assert two opens get distinct ones. */
  private static final class RecordingFactory implements InventoryFactory {
    private int created;

    @Override
    public Inventory create(InventoryHolder holder, int size, Component title) {
      created++;
      return inventoryOfSize(size, holder);
    }

    int created() {
      return created;
    }
  }

  /** A {@link MenuScheduler} recording which player ids it resolved schedulers for. */
  private static final class RecordingScheduler implements MenuScheduler {
    private final List<UUID> resolved = new ArrayList<>();

    @Override
    public PlayerScheduler forPlayer(dev.haniel.menu.domain.PlayerId player) {
      resolved.add(player.value());
      return new PlayerScheduler() {
        @Override
        public ScheduledTask schedule(Runnable task) {
          return noOpTask();
        }

        @Override
        public ScheduledTask scheduleRepeating(Runnable task, long period) {
          return noOpTask();
        }
      };
    }

    @Override
    public java.util.concurrent.Executor global() {
      return Runnable::run;
    }

    List<UUID> resolved() {
      return resolved;
    }

    private static ScheduledTask noOpTask() {
      return new ScheduledTask() {
        @Override
        public void cancel() {}

        @Override
        public boolean scheduled() {
          return true;
        }
      };
    }
  }
}
