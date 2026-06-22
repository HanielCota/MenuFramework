package com.hanielfialho.menuframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuCloseReason;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuOpenContext;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.internal.lifecycle.MenuHistoryRegistry;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
final class MenuNavigationHistoryTest extends MenuManagerTestSupport {

  @Test
  void rootMenuStartsWithoutHistory() {
    ContextMenu root = new ContextMenu("Root");

    this.menus.open(this.player, root, MenuState.initial());
    this.advanceTicks(2L);

    assertEquals(0, this.menus.historyDepth(this.player));
    assertFalse(this.menus.canGoBack(this.player));
    assertFalse(this.menus.back(this.player));
    assertEquals(0, root.renderHistoryDepth());
    assertEquals(0, root.openHistoryDepth());
    assertFalse(root.renderCanGoBack());
    assertFalse(root.openCanGoBack());
  }

  @Test
  void destinationContextsExposeTheCommittedHistoryDepth() {
    RecordingMenu source = new RecordingMenu("Source");
    ContextMenu destination = new ContextMenu("Destination");

    source.onPrimary(interaction -> interaction.open(destination, new MenuState(4)));

    this.open(source, MenuState.initial());
    this.dispatchPrimaryClick();
    this.advanceTicks(2L);

    assertEquals(1, destination.renderHistoryDepth());
    assertEquals(1, destination.openHistoryDepth());
    assertTrue(destination.renderCanGoBack());
    assertTrue(destination.openCanGoBack());
  }

  @Test
  void forwardNavigationPushesSourceAndBackRestoresItsState() {
    RecordingMenu first = new RecordingMenu("First");
    RecordingMenu second = new RecordingMenu("Second");

    this.open(first, MenuState.initial());
    this.dispatchPrimaryClick();
    assertEquals(Material.DIAMOND, this.primaryMaterial());

    first.onPrimary(interaction -> interaction.open(second, new MenuState(7)));

    this.dispatchPrimaryClick();
    this.advanceTicks(2L);

    var secondSessionId = this.currentSessionId();

    assertEquals(1, this.menus.historyDepth(this.player));
    assertTrue(this.menus.canGoBack(this.player));
    assertEquals(List.of(MenuCloseReason.NAVIGATION), first.closeReasons());

    second.onPrimary(interaction -> interaction.back());
    this.dispatchPrimaryClick();
    this.advanceTicks(2L);

    assertNotEquals(secondSessionId, this.currentSessionId());
    assertEquals(0, this.menus.historyDepth(this.player));
    assertFalse(this.menus.canGoBack(this.player));
    assertEquals(2, first.openCount());
    assertEquals(new MenuState(1), first.openedState());
    assertEquals(Material.DIAMOND, this.primaryMaterial());
    assertEquals(List.of(MenuCloseReason.BACK), second.closeReasons());
  }

  @Test
  void managerBackUsesTheSameHistoryTransition() {
    RecordingMenu first = new RecordingMenu("First");
    RecordingMenu second = new RecordingMenu("Second");

    first.onPrimary(interaction -> interaction.open(second, new MenuState(3)));

    this.open(first, MenuState.initial());
    this.dispatchPrimaryClick();
    this.advanceTicks(2L);

    assertTrue(this.menus.back(this.player));
    this.advanceTicks(2L);

    assertEquals(2, first.openCount());
    assertEquals(0, this.menus.historyDepth(this.player));
    assertEquals(List.of(MenuCloseReason.BACK), second.closeReasons());
  }

  @Test
  void historyIsLifoAcrossMultipleMenus() {
    RecordingMenu first = new RecordingMenu("First");
    RecordingMenu second = new RecordingMenu("Second");
    RecordingMenu third = new RecordingMenu("Third");

    first.onPrimary(interaction -> interaction.open(second, new MenuState(2)));
    second.onPrimary(interaction -> interaction.open(third, new MenuState(3)));

    this.open(first, new MenuState(1));
    this.dispatchPrimaryClick();
    this.advanceTicks(2L);
    this.dispatchPrimaryClick();
    this.advanceTicks(2L);

    assertEquals(2, this.menus.historyDepth(this.player));

    assertTrue(this.menus.back(this.player));
    this.advanceTicks(2L);

    assertEquals(1, this.menus.historyDepth(this.player));
    assertEquals(new MenuState(2), second.openedState());

    assertTrue(this.menus.back(this.player));
    this.advanceTicks(2L);

    assertEquals(0, this.menus.historyDepth(this.player));
    assertEquals(new MenuState(1), first.openedState());
    assertFalse(this.menus.back(this.player));
  }

  @Test
  void externalOpenStartsANewHistoryRoot() {
    RecordingMenu first = new RecordingMenu("First");
    RecordingMenu second = new RecordingMenu("Second");
    RecordingMenu replacement = new RecordingMenu("Replacement");

    first.onPrimary(interaction -> interaction.open(second, MenuState.initial()));

    this.open(first, MenuState.initial());
    this.dispatchPrimaryClick();
    this.advanceTicks(2L);

    assertEquals(1, this.menus.historyDepth(this.player));

    assertTrue(this.menus.open(this.player, replacement, new MenuState(9)));
    this.advanceTicks(2L);

    assertEquals(0, this.menus.historyDepth(this.player));
    assertFalse(this.menus.canGoBack(this.player));
    assertEquals(List.of(MenuCloseReason.REPLACED), second.closeReasons());
  }

  @Test
  void failedTargetRenderPreservesCurrentMenuAndHistory() {
    RecordingMenu first = new RecordingMenu("First");
    RecordingMenu second = new RecordingMenu("Second");
    FailingMenu failing = new FailingMenu();

    first.onPrimary(interaction -> interaction.open(second, new MenuState(2)));

    this.open(first, MenuState.initial());
    this.dispatchPrimaryClick();
    this.advanceTicks(2L);

    var currentSessionId = this.currentSessionId();
    second.onPrimary(interaction -> interaction.open(failing, MenuState.initial()));

    this.dispatchPrimaryClick();
    this.advanceTicks(2L);

    assertTrue(this.menus.isOpen(this.player));
    assertEquals(currentSessionId, this.currentSessionId());
    assertEquals(1, this.menus.historyDepth(this.player));
    assertEquals(0, second.closeCount());
  }

  @Test
  void branchAfterBackDoesNotRestoreTheDiscardedScreen() {
    RecordingMenu first = new RecordingMenu("First");
    RecordingMenu second = new RecordingMenu("Second");
    RecordingMenu third = new RecordingMenu("Third");
    RecordingMenu branch = new RecordingMenu("Branch");

    first.onPrimary(interaction -> interaction.open(second, new MenuState(2)));
    second.onPrimary(interaction -> interaction.open(third, new MenuState(3)));

    this.open(first, new MenuState(1));
    this.dispatchPrimaryClick();
    this.advanceTicks(2L);
    this.dispatchPrimaryClick();
    this.advanceTicks(2L);

    assertTrue(this.menus.back(this.player));
    this.advanceTicks(2L);

    second.onPrimary(interaction -> interaction.open(branch, new MenuState(4)));
    this.dispatchPrimaryClick();
    this.advanceTicks(2L);

    assertEquals(2, this.menus.historyDepth(this.player));

    assertTrue(this.menus.back(this.player));
    this.advanceTicks(2L);
    assertEquals(new MenuState(2), second.openedState());

    assertTrue(this.menus.back(this.player));
    this.advanceTicks(2L);
    assertEquals(new MenuState(1), first.openedState());

    assertEquals(1, third.openCount());
    assertEquals(1, branch.openCount());
    assertEquals(0, this.menus.historyDepth(this.player));
  }

  @Test
  void historyIsBoundedToTheConfiguredDefaultDepth() {
    AtomicReference<RecordingMenu> reference = new AtomicReference<>();
    RecordingMenu menu = new RecordingMenu("Loop");
    reference.set(menu);

    menu.onPrimary(
        interaction -> interaction.open(reference.get(), interaction.state().increment()));

    this.open(menu, MenuState.initial());

    for (int index = 0; index < 40; index++) {
      this.dispatchPrimaryClick();
      this.advanceTicks(2L);
    }

    assertEquals(MenuHistoryRegistry.DEFAULT_MAX_DEPTH, this.menus.historyDepth(this.player));
    assertEquals(new MenuState(40), menu.openedState());

    for (int index = 0; index < MenuHistoryRegistry.DEFAULT_MAX_DEPTH; index++) {
      assertTrue(this.menus.back(this.player));
      this.advanceTicks(2L);
    }

    assertEquals(0, this.menus.historyDepth(this.player));
    assertEquals(new MenuState(8), menu.openedState());
    assertFalse(this.menus.back(this.player));
  }

  @Test
  void terminalCloseClearsTheWholeHistory() {
    RecordingMenu first = new RecordingMenu("First");
    RecordingMenu second = new RecordingMenu("Second");

    first.onPrimary(interaction -> interaction.open(second, MenuState.initial()));

    this.open(first, MenuState.initial());
    this.dispatchPrimaryClick();
    this.advanceTicks(2L);

    assertEquals(1, this.menus.historyDepth(this.player));

    assertTrue(this.menus.close(this.player));
    this.advanceTicks(2L);

    assertEquals(0, this.menus.historyDepth(this.player));
    assertFalse(this.menus.canGoBack(this.player));
  }

  private Material primaryMaterial() {
    return Objects.requireNonNull(
            this.player.getOpenInventory().getTopInventory().getItem(PRIMARY_SLOT))
        .getType();
  }

  private static final class ContextMenu implements Menu<MenuState> {

    private static final MenuLayout LAYOUT = MenuLayout.chest(1);

    private final String title;
    private final AtomicInteger renderDepth;
    private final AtomicInteger openDepth;
    private final AtomicBoolean renderCanGoBack;
    private final AtomicBoolean openCanGoBack;

    private ContextMenu(String title) {
      this.title = Objects.requireNonNull(title, "title");
      this.renderDepth = new AtomicInteger(-1);
      this.openDepth = new AtomicInteger(-1);
      this.renderCanGoBack = new AtomicBoolean();
      this.openCanGoBack = new AtomicBoolean();
    }

    int renderHistoryDepth() {
      return this.renderDepth.get();
    }

    int openHistoryDepth() {
      return this.openDepth.get();
    }

    boolean renderCanGoBack() {
      return this.renderCanGoBack.get();
    }

    boolean openCanGoBack() {
      return this.openCanGoBack.get();
    }

    @Override
    public MenuLayout layout() {
      return LAYOUT;
    }

    @Override
    public Component title(@NonNull MenuRenderContext<MenuState> context) {
      return Component.text(this.title);
    }

    @Override
    public void render(MenuRenderContext<MenuState> context, MenuCanvas<MenuState> canvas) {
      this.renderDepth.set(context.historyDepth());
      this.renderCanGoBack.set(context.canGoBack());
      canvas.item(0, new ItemStack(Material.STONE));
    }

    @Override
    public void onOpen(MenuOpenContext<MenuState> context) {
      this.openDepth.set(context.historyDepth());
      this.openCanGoBack.set(context.canGoBack());
    }
  }

  private static final class FailingMenu implements Menu<MenuState> {

    @Override
    public MenuLayout layout() {
      return MenuLayout.chest(1);
    }

    @Override
    public Component title(@NonNull MenuRenderContext<MenuState> context) {
      return Component.text("Failing");
    }

    @Override
    public void render(MenuRenderContext<MenuState> context, MenuCanvas<MenuState> canvas) {
      throw new IllegalStateException("expected render failure");
    }
  }
}
