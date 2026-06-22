package com.hanielfialho.menuframework;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.menuframework.api.InteractionPolicy;
import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuClick;
import com.hanielfialho.menuframework.api.MenuClickHandler;
import com.hanielfialho.menuframework.api.MenuCloseReason;
import com.hanielfialho.menuframework.api.MenuContext;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuOpenContext;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.internal.inventory.MenuHolder;
import com.hanielfialho.menuframework.internal.inventory.MenuViewAccess;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

abstract class MenuManagerTestSupport {

  static final int PRIMARY_SLOT = 11;
  static final int CLOSE_SLOT = 15;

  ServerMock server;
  Plugin plugin;
  MenuFramework framework;
  MenuManager menus;
  PlayerMock player;

  @BeforeEach
  void setUpFramework() {
    this.server = MockBukkit.mock();
    this.plugin = MockBukkit.createMockPlugin();
    this.framework = MenuFramework.create(this.plugin);
    this.menus = this.framework.menus();
    this.player = this.server.addPlayer();
  }

  @AfterEach
  void tearDownFramework() {
    if (this.framework != null) {
      this.framework.shutdown();
    }

    MockBukkit.unmock();
  }

  void open(RecordingMenu menu, MenuState initialState) {
    assertTrue(this.menus.open(this.player, menu, initialState));

    this.advanceTicks(2L);
  }

  void advanceTicks(long ticks) {
    this.server.getScheduler().performTicks(ticks);
  }

  void waitAsyncTasks() {
    this.server.getScheduler().waitAsyncTasksFinished();
  }

  UUID currentSessionId() {
    MenuHolder menuHolder =
        assertInstanceOf(
            MenuHolder.class,
            MenuViewAccess.holderOf(this.player.getOpenInventory().getTopInventory()));

    return menuHolder.sessionId();
  }

  void dispatchPrimaryClick() {
    this.menus.dispatchClick(this.player, this.currentSessionId(), leftClick(PRIMARY_SLOT));
  }

  void dispatchCloseClick() {
    this.menus.dispatchClick(this.player, this.currentSessionId(), leftClick(CLOSE_SLOT));
  }

  static MenuClick leftClick(int rawSlot) {
    return new MenuClick(
        rawSlot, ClickType.LEFT, InventoryAction.PICKUP_ALL, MenuClick.NO_HOTBAR_BUTTON);
  }

  record MenuState(int value) {

    MenuState {
      if (value < 0) {
        throw new IllegalArgumentException("value must be >= 0: " + value);
      }
    }

    static MenuState initial() {
      return new MenuState(0);
    }

    MenuState increment() {
      return new MenuState(Math.incrementExact(this.value));
    }
  }

  static final class RecordingMenu implements Menu<MenuState> {

    private static final MenuLayout LAYOUT = MenuLayout.chest(3);

    private final String name;
    private final InteractionPolicy interactionPolicy;
    private final AtomicInteger openCount;
    private final AtomicInteger closeCount;
    private final AtomicReference<MenuState> openedState;
    private final AtomicReference<MenuState> closedState;
    private final AtomicReference<UUID> openedSessionId;
    private final List<MenuCloseReason> closeReasons;

    private MenuClickHandler<MenuState> primaryHandler;

    RecordingMenu(String name) {
      this(name, InteractionPolicy.READ_ONLY);
    }

    RecordingMenu(String name, InteractionPolicy interactionPolicy) {
      this.name = Objects.requireNonNull(name, "name");
      this.interactionPolicy = Objects.requireNonNull(interactionPolicy, "interactionPolicy");

      this.openCount = new AtomicInteger();
      this.closeCount = new AtomicInteger();
      this.openedState = new AtomicReference<>();
      this.closedState = new AtomicReference<>();
      this.openedSessionId = new AtomicReference<>();
      this.closeReasons = new CopyOnWriteArrayList<>();

      this.primaryHandler = interaction -> interaction.updateState(MenuState::increment);
    }

    void onPrimary(MenuClickHandler<MenuState> primaryHandler) {
      this.primaryHandler = Objects.requireNonNull(primaryHandler, "primaryHandler");
    }

    int openCount() {
      return this.openCount.get();
    }

    int closeCount() {
      return this.closeCount.get();
    }

    MenuState openedState() {
      return this.openedState.get();
    }

    MenuState closedState() {
      return this.closedState.get();
    }

    UUID openedSessionId() {
      return this.openedSessionId.get();
    }

    List<MenuCloseReason> closeReasons() {
      return List.copyOf(this.closeReasons);
    }

    @Override
    public MenuLayout layout() {
      return LAYOUT;
    }

    @Override
    public InteractionPolicy interactionPolicy() {
      return this.interactionPolicy;
    }

    @Override
    public Component title(@NonNull MenuRenderContext<MenuState> context) {
      return Component.text(this.name);
    }

    @Override
    public void render(MenuRenderContext<MenuState> context, MenuCanvas<MenuState> canvas) {
      Material material = context.state().value() == 0 ? Material.STONE : Material.DIAMOND;

      canvas.button(
          PRIMARY_SLOT,
          new ItemStack(material),
          interaction -> this.primaryHandler.handle(interaction));

      canvas.button(
          CLOSE_SLOT, new ItemStack(Material.BARRIER), interaction -> interaction.close());
    }

    @Override
    public void onOpen(MenuOpenContext<MenuState> context) {
      this.openCount.incrementAndGet();
      this.openedState.set(context.state());
      this.openedSessionId.set(context.sessionId());
    }

    @Override
    public void onClose(MenuContext<MenuState> context, MenuCloseReason reason) {
      this.closeCount.incrementAndGet();
      this.closedState.set(context.state());
      this.closeReasons.add(reason);
    }
  }
}
