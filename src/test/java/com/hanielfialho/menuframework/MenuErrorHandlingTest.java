package com.hanielfialho.menuframework;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.menuframework.api.EmptyMenuState;
import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuClick;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.error.DefaultMenuErrorHandler;
import com.hanielfialho.menuframework.api.error.MenuErrorHandler;
import com.hanielfialho.menuframework.api.error.MenuFailureContext;
import com.hanielfialho.menuframework.api.error.MenuFailureOperation;
import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import com.hanielfialho.menuframework.internal.inventory.MenuHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

@Execution(ExecutionMode.SAME_THREAD)
final class MenuErrorHandlingTest {

  private static final int BUTTON_SLOT = 4;

  private ServerMock server;
  private Plugin plugin;
  private PlayerMock player;
  private MenuFramework framework;

  @BeforeEach
  void setUp() {
    this.server = MockBukkit.mock();
    this.plugin = MockBukkit.createMockPlugin();
    this.player = this.server.addPlayer();
  }

  @AfterEach
  void tearDown() {
    if (this.framework != null) {
      this.framework.shutdown();
    }

    MockBukkit.unmock();
  }

  @Test
  void configuredHandlerReceivesOpenFailure() {
    List<MenuFailureContext> failures = new ArrayList<>();
    this.createFramework(failures::add);

    RuntimeException expected = new RuntimeException("render");
    FailingRenderMenu menu = new FailingRenderMenu(expected);

    assertTrue(this.framework.menus().open(this.player, menu, EmptyMenuState.INSTANCE));

    this.server.getScheduler().performTicks(2L);

    assertEquals(1, failures.size());

    MenuFailureContext failure = failures.getFirst();
    assertEquals(MenuFailureOperation.OPEN, failure.operation());
    assertSame(expected, failure.cause());
    assertEquals(this.player.getUniqueId(), failure.viewerId());
    assertEquals(FailingRenderMenu.class, failure.menuType());
    assertFalse(failure.hasSession());
    assertFalse(failure.hasTask());
    assertFalse(this.framework.menus().isOpen(this.player));
  }

  @Test
  void clickFailureIncludesSessionSnapshot() {
    List<MenuFailureContext> failures = new ArrayList<>();
    this.createFramework(failures::add);

    RuntimeException expected = new RuntimeException("click");
    ClickFailureMenu menu = new ClickFailureMenu(expected);

    assertTrue(this.framework.menus().open(this.player, menu, EmptyMenuState.INSTANCE));
    this.server.getScheduler().performTicks(2L);

    MenuHolder holder =
        assertInstanceOf(
            MenuHolder.class, this.player.getOpenInventory().getTopInventory().getHolder(false));
    UUID sessionId = holder.sessionId();
    assertNotNull(sessionId);

    this.framework
        .menus()
        .dispatchClick(
            this.player,
            sessionId,
            new MenuClick(
                BUTTON_SLOT,
                ClickType.LEFT,
                InventoryAction.PICKUP_ALL,
                MenuClick.NO_HOTBAR_BUTTON));

    assertEquals(1, failures.size());

    MenuFailureContext failure = failures.getFirst();
    assertEquals(MenuFailureOperation.CLICK_HANDLER, failure.operation());
    assertSame(expected, failure.cause());
    assertEquals(holder.sessionId(), failure.sessionId().orElseThrow());
    assertEquals(1L, failure.revision().orElseThrow());
    assertFalse(failure.hasTask());
    assertTrue(this.framework.menus().isOpen(this.player));
  }

  @Test
  void errorHandlerFailureDoesNotEscapeIntoRuntime() {
    this.createFramework(
        context -> {
          throw new IllegalStateException("handler");
        });

    assertTrue(
        this.framework
            .menus()
            .open(
                this.player,
                new FailingRenderMenu(new RuntimeException("original")),
                EmptyMenuState.INSTANCE));

    assertDoesNotThrow(() -> this.server.getScheduler().performTicks(2L));

    assertFalse(this.framework.menus().isOpen(this.player));
  }

  @Test
  void failureContextExposesTaskMetadata() {
    UUID viewerId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    MenuTaskKey taskKey = MenuTaskKey.of("products");

    MenuFailureContext context =
        MenuFailureContext.builder(
                MenuFailureOperation.PERIODIC_EXECUTION,
                new RuntimeException("periodic"),
                viewerId,
                ClickFailureMenu.class)
            .session(sessionId, 7L)
            .task(taskKey, 3L, 11L)
            .build();

    assertEquals(sessionId, context.sessionId().orElseThrow());
    assertEquals(7L, context.revision().orElseThrow());
    assertEquals(taskKey, context.taskKey().orElseThrow());
    assertEquals(3L, context.taskGeneration().orElseThrow());
    assertEquals(11L, context.taskExecution().orElseThrow());

    String formatted = DefaultMenuErrorHandler.format(context);
    assertTrue(formatted.contains("PERIODIC_EXECUTION"));
    assertTrue(formatted.contains("task=products"));
    assertTrue(formatted.contains("generation=3"));
    assertTrue(formatted.contains("execution=11"));
  }

  @Test
  void configurationCanBeCopiedWithoutLosingHandler() {
    MenuFrameworkConfiguration configured =
        MenuFrameworkConfiguration.builder().errorHandler(context -> {}).build();

    assertTrue(configured.errorHandler().isPresent());
    assertTrue(configured.toBuilder().build().errorHandler().isPresent());
    assertTrue(configured.toBuilder().useDefaultErrorHandler().build().errorHandler().isEmpty());
    assertTrue(MenuFrameworkConfiguration.defaults().errorHandler().isEmpty());
    assertEquals(
        MenuFrameworkConfiguration.DEFAULT_MAX_NAVIGATION_HISTORY_DEPTH,
        configured.maxNavigationHistoryDepth());
  }

  private void createFramework(MenuErrorHandler handler) {
    this.framework =
        MenuFramework.create(
            this.plugin, MenuFrameworkConfiguration.builder().errorHandler(handler).build());
  }

  private record FailingRenderMenu(RuntimeException failure) implements Menu<EmptyMenuState> {

    @Override
    public MenuLayout layout() {
      return MenuLayout.chest(1);
    }

    @Override
    public Component title(MenuRenderContext<EmptyMenuState> context) {
      return Component.text("Failure");
    }

    @Override
    public void render(
        MenuRenderContext<EmptyMenuState> context, MenuCanvas<EmptyMenuState> canvas) {
      throw this.failure;
    }
  }

  private record ClickFailureMenu(RuntimeException failure) implements Menu<EmptyMenuState> {

    @Override
    public MenuLayout layout() {
      return MenuLayout.chest(1);
    }

    @Override
    public Component title(MenuRenderContext<EmptyMenuState> context) {
      return Component.text("Click failure");
    }

    @Override
    public void render(
        MenuRenderContext<EmptyMenuState> context, MenuCanvas<EmptyMenuState> canvas) {
      canvas.button(
          BUTTON_SLOT,
          new ItemStack(Material.STONE),
          interaction -> {
            throw this.failure;
          });
    }
  }
}
