package dev.haniel.menu.paper.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.binding.UnboundProvider;
import dev.haniel.menu.compiler.model.CompiledPagedMenu;
import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.paper.render.InventoryFactory;
import dev.haniel.menu.scheduler.MenuScheduler;
import dev.haniel.menu.scheduler.PlayerScheduler;
import dev.haniel.menu.scheduler.ScheduledTask;
import dev.haniel.menu.template.IconFactory;
import dev.haniel.menu.template.PagedAppearance;
import dev.haniel.menu.template.PagedDecor;
import dev.haniel.menu.template.PagedWiring;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

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

  private static CompiledPagedMenu<ItemStack> pagedPlan(Instantiator instantiator) {
    PagedAppearance<ItemStack> appearance =
        new PagedAppearance<>(
            new MenuId("paged"),
            "<title>",
            MaskLayout.resolve(List.of("<X>      "), 1),
            new PagedDecor<>(null, null, null),
            Map.of());
    PagedWiring wiring = new PagedWiring(instantiator, provider(), Map.of(), List.of());
    return new CompiledPagedMenu<>(appearance, wiring);
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
      return task -> noOpTask();
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
