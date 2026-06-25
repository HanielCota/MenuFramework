package com.hanielfialho.menuframework.testing;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuClick;
import com.hanielfialho.menuframework.api.MenuClickHandler;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.feedback.MenuFeedbackSignal;
import com.hanielfialho.menuframework.api.task.MenuPeriodicTask;
import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import com.hanielfialho.menuframework.api.task.MenuTickContext;
import com.hanielfialho.menuframework.api.task.MenuTickResult;
import com.hanielfialho.menuframework.api.theme.MenuTheme;
import com.hanielfialho.menuframework.internal.interaction.MenuInteractionImpl;
import com.hanielfialho.menuframework.internal.lifecycle.MenuNavigation;
import com.hanielfialho.menuframework.internal.render.MenuFrame;
import com.hanielfialho.menuframework.internal.render.MenuRenderer;
import com.hanielfialho.menuframework.internal.render.MenuSlot;
import com.hanielfialho.menuframework.internal.task.MenuAsyncCommand;
import com.hanielfialho.menuframework.internal.task.MenuPeriodicCommand;
import com.hanielfialho.menuframework.internal.task.MenuTaskCommands;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Deterministic test harness for rendering and invoking menu handlers without opening an inventory.
 *
 * <p>The harness has no JUnit dependency. Supply a player implementation from MockBukkit or another
 * controlled test environment. It executes synchronous state transitions, captures terminal and
 * task commands, and applies an asynchronous operation's start transition without starting its
 * background work.
 *
 * <p>This class intentionally does not emulate Bukkit inventory events, entity scheduling, session
 * replacement or asynchronous completion. Those remain integration-test responsibilities.
 *
 * @param <S> menu-state type
 */
public final class MenuTestHarness<S> {

  private final java.util.UUID sessionId;
  private final Menu<S> menu;
  private final Player viewer;
  private final int historyDepth;
  private final MenuTheme theme;
  private final Component title;
  private final Map<MenuTaskKey, Long> taskGenerations;
  private final Map<MenuTaskKey, PendingAsync<S, ?>> pendingAsync;
  private final Map<MenuTaskKey, ActivePeriodic<S>> activePeriodics;

  private S state;
  private MenuFrame<S> frame;
  private long revision;
  private boolean terminated;
  private boolean closed;
  private MenuTestOutcome<S> lastOutcome;

  private record PendingAsync<S, R>(
      MenuAsyncCommand<S, R> command, long generation, S stateAtStart) {}

  private record ActivePeriodic<S>(MenuTaskKey key, MenuPeriodicTask<S> task) {}

  private MenuTestHarness(Builder<S> builder) {
    this.sessionId = java.util.UUID.randomUUID();
    this.menu = builder.menu;
    this.viewer = builder.viewer;
    this.state = builder.initialState;
    this.historyDepth = builder.historyDepth;
    this.theme =
        Objects.requireNonNull(
            this.menu.theme(builder.defaultTheme), "The menu returned a null theme");
    MenuRenderContext<S> initialContext =
        new MenuRenderContext<>(this.viewer, this.state, this.historyDepth, this.theme);
    this.title =
        Objects.requireNonNull(this.menu.title(initialContext), "The menu returned a null title");
    this.taskGenerations = new HashMap<>();
    this.pendingAsync = new HashMap<>();
    this.activePeriodics = new HashMap<>();
    this.frame =
        MenuRenderer.render(this.menu, this.viewer, this.state, this.historyDepth, this.theme);
    this.revision = 1L;
  }

  /**
   * Creates a harness using the default theme and no navigation history.
   *
   * @param menu menu under test
   * @param viewer controlled player
   * @param initialState initial state
   * @param <S> menu-state type
   * @return initialized harness
   */
  public static <S> MenuTestHarness<S> create(Menu<S> menu, Player viewer, S initialState) {
    return builder(menu, viewer, initialState).build();
  }

  /**
   * Creates a configurable harness builder.
   *
   * @param menu menu under test
   * @param viewer controlled player
   * @param initialState initial state
   * @param <S> menu-state type
   * @return builder
   */
  public static <S> Builder<S> builder(Menu<S> menu, Player viewer, S initialState) {
    return new Builder<>(menu, viewer, initialState);
  }

  /**
   * Returns the synthetic session id used by this harness.
   *
   * @return harness session id
   */
  public java.util.UUID sessionId() {
    return this.sessionId;
  }

  /**
   * Returns the menu under test.
   *
   * @return menu
   */
  public Menu<S> menu() {
    return this.menu;
  }

  /**
   * Returns the rendered layout.
   *
   * @return layout
   */
  public MenuLayout layout() {
    return this.frame.layout();
  }

  /**
   * Returns the structural title resolved during harness creation.
   *
   * @return non-null Adventure title
   */
  public Component title() {
    return this.title;
  }

  /**
   * Returns the current harness state.
   *
   * @return state
   */
  public S state() {
    return this.state;
  }

  /**
   * Returns the current positive harness revision.
   *
   * @return revision
   */
  public long revision() {
    return this.revision;
  }

  /**
   * Returns whether a tested handler requested a terminal command.
   *
   * <p>This includes close, back navigation and forward navigation.
   *
   * @return terminal-command flag
   */
  public boolean terminated() {
    return this.terminated;
  }

  /**
   * Returns whether a tested handler specifically requested close.
   *
   * @return close-request flag
   */
  public boolean closed() {
    return this.closed;
  }

  /**
   * Returns the most recent click outcome.
   *
   * @return optional outcome
   */
  public Optional<MenuTestOutcome<S>> lastOutcome() {
    return Optional.ofNullable(this.lastOutcome);
  }

  /**
   * Returns a defensive copy of the rendered item at a raw slot.
   *
   * @param slot raw slot
   * @return optional item
   */
  public Optional<ItemStack> item(int slot) {
    MenuSlot<S> menuSlot = this.frame.slotOrNull(slot);
    return menuSlot == null ? Optional.empty() : Optional.of(menuSlot.icon());
  }

  /**
   * Returns a defensive copy of the item at a named slot.
   *
   * @param namedSlot named slot
   * @return optional item
   */
  public Optional<ItemStack> item(String namedSlot) {
    return this.item(this.layout().slot(namedSlot));
  }

  /**
   * Returns whether a raw slot has a click handler.
   *
   * @param slot raw slot
   * @return clickable flag
   */
  public boolean clickable(int slot) {
    MenuSlot<S> menuSlot = this.frame.slotOrNull(slot);
    return menuSlot != null && menuSlot.clickable();
  }

  /**
   * Returns whether a named slot has a click handler.
   *
   * @param namedSlot named slot
   * @return clickable flag
   */
  public boolean clickable(String namedSlot) {
    return this.clickable(this.layout().slot(namedSlot));
  }

  /**
   * Publishes a fresh frame using the current state.
   *
   * @return this harness
   */
  public MenuTestHarness<S> refresh() {
    this.ensureOpen();
    this.publish(this.state);
    return this;
  }

  /**
   * Invokes a button using a default pickup action.
   *
   * @param slot raw top-inventory slot
   * @param clickType click type
   * @return click outcome
   */
  public MenuTestOutcome<S> click(int slot, ClickType clickType) {
    return this.click(
        new MenuClick(
            slot,
            Objects.requireNonNull(clickType, "clickType"),
            InventoryAction.PICKUP_ALL,
            MenuClick.NO_HOTBAR_BUTTON));
  }

  /**
   * Invokes a named button using a default pickup action.
   *
   * @param namedSlot named slot
   * @param clickType click type
   * @return click outcome
   */
  public MenuTestOutcome<S> click(String namedSlot, ClickType clickType) {
    return this.click(this.layout().slot(namedSlot), clickType);
  }

  /**
   * Invokes a rendered button and synchronously interprets its command buffer.
   *
   * @param click immutable click snapshot
   * @return click outcome
   */
  public MenuTestOutcome<S> click(MenuClick click) {
    Objects.requireNonNull(click, "click");

    this.ensureOpen();

    MenuSlot<S> slot = this.frame.slotOrNull(click.rawSlot());
    if (slot == null || !slot.clickable()) {
      throw new IllegalArgumentException(
          "Slot " + click.rawSlot() + " is not a clickable menu button");
    }

    MenuClickHandler<S> handler = slot.clickHandler().orElseThrow();
    MenuInteractionImpl<S> interaction =
        new MenuInteractionImpl<>(
            this.sessionId, this.viewer, this.state, this.revision, this.historyDepth, click);

    try {
      handler.handle(interaction);
    } finally {
      interaction.finish();
    }

    boolean rendered = false;
    boolean closeRequested = interaction.closeRequested();
    boolean backRequested = interaction.backRequested();
    MenuNavigation<?> navigationRequest = interaction.navigationRequest();
    MenuAsyncCommand<S, ?> asyncCommand = interaction.asyncCommand();

    if (closeRequested) {
      this.closed = true;
      this.terminated = true;
    } else if (backRequested || navigationRequest != null) {
      this.terminated = true;
    } else {
      if (asyncCommand != null) {
        this.applyAsyncStart(asyncCommand);
        rendered = true;
      } else if (interaction.refreshRequested()) {
        this.publish(interaction.resultingState());
        rendered = true;
      }
    }

    MenuTaskCommands<S> taskCommands = interaction.taskCommands();
    List<MenuTaskKey> periodicTasks =
        taskCommands.periodicCommands().stream().map(MenuPeriodicCommand::key).toList();

    for (MenuTaskKey key : periodicTasks) {
      this.nextTaskGeneration(key);
      MenuPeriodicCommand<S> command =
          taskCommands.periodicCommands().stream()
              .filter(c -> c.key().equals(key))
              .findFirst()
              .orElseThrow();
      this.activePeriodics.put(key, new ActivePeriodic<>(key, command.task()));
    }

    Optional<MenuTestNavigation> navigation =
        navigationRequest == null
            ? Optional.empty()
            : Optional.of(
                new MenuTestNavigation(navigationRequest.menu(), navigationRequest.initialState()));

    this.lastOutcome =
        new MenuTestOutcome<>(
            this.state,
            this.revision,
            rendered,
            closeRequested,
            backRequested,
            navigation,
            Optional.ofNullable(asyncCommand).map(MenuAsyncCommand::key),
            periodicTasks,
            taskCommands.cancellations(),
            interaction.feedbackSignals());
    return this.lastOutcome;
  }

  /**
   * Asserts that a raw slot contains the expected material.
   *
   * @param slot raw slot
   * @param material expected material
   * @return this harness
   * @throws AssertionError when the assertion fails
   */
  public MenuTestHarness<S> assertItem(int slot, Material material) {
    Material actual = this.item(slot).map(ItemStack::getType).orElse(Material.AIR);
    if (actual != Objects.requireNonNull(material, "material")) {
      throw new AssertionError(
          "Expected slot " + slot + " to contain " + material + " but found " + actual);
    }
    return this;
  }

  /**
   * Asserts that a named slot contains the expected material.
   *
   * @param namedSlot named slot
   * @param material expected material
   * @return this harness
   */
  public MenuTestHarness<S> assertItem(String namedSlot, Material material) {
    return this.assertItem(this.layout().slot(namedSlot), material);
  }

  /**
   * Asserts that a raw slot is empty.
   *
   * @param slot raw slot
   * @return this harness
   */
  public MenuTestHarness<S> assertEmpty(int slot) {
    if (this.item(slot).isPresent()) {
      throw new AssertionError("Expected slot " + slot + " to be empty");
    }
    return this;
  }

  /**
   * Asserts that a named slot is empty.
   *
   * @param namedSlot named slot
   * @return this harness
   */
  public MenuTestHarness<S> assertEmpty(String namedSlot) {
    return this.assertEmpty(this.layout().slot(namedSlot));
  }

  /**
   * Asserts that a raw slot is clickable.
   *
   * @param slot raw slot
   * @return this harness
   */
  public MenuTestHarness<S> assertClickable(int slot) {
    if (!this.clickable(slot)) {
      throw new AssertionError("Expected slot " + slot + " to be clickable");
    }
    return this;
  }

  /**
   * Asserts that a named slot is clickable.
   *
   * @param namedSlot named slot
   * @return this harness
   */
  public MenuTestHarness<S> assertClickable(String namedSlot) {
    return this.assertClickable(this.layout().slot(namedSlot));
  }

  /**
   * Asserts that a raw slot is not clickable.
   *
   * @param slot raw slot
   * @return this harness
   */
  public MenuTestHarness<S> assertNotClickable(int slot) {
    if (this.clickable(slot)) {
      throw new AssertionError("Expected slot " + slot + " not to be clickable");
    }
    return this;
  }

  /**
   * Asserts that a named slot is not clickable.
   *
   * @param namedSlot named slot
   * @return this harness
   */
  public MenuTestHarness<S> assertNotClickable(String namedSlot) {
    return this.assertNotClickable(this.layout().slot(namedSlot));
  }

  /**
   * Asserts the display name of the item at a raw slot.
   *
   * @param slot raw slot
   * @param expected expected plain-text display name
   * @return this harness
   */
  public MenuTestHarness<S> assertDisplayName(int slot, String expected) {
    ItemStack icon = this.requireItem(slot);
    String actual = plainName(icon);
    if (!Objects.requireNonNull(expected, "expected").equals(actual)) {
      throw new AssertionError(
          "Expected slot " + slot + " display name '" + expected + "' but found '" + actual + "'");
    }
    return this;
  }

  /**
   * Asserts the display name of the item at a named slot.
   *
   * @param namedSlot named slot
   * @param expected expected plain-text display name
   * @return this harness
   */
  public MenuTestHarness<S> assertDisplayName(String namedSlot, String expected) {
    return this.assertDisplayName(this.layout().slot(namedSlot), expected);
  }

  /**
   * Asserts that the item at a raw slot matches the supplied predicate.
   *
   * @param slot raw slot
   * @param predicate item predicate
   * @param message assertion message
   * @return this harness
   */
  public MenuTestHarness<S> assertItem(
      int slot, Predicate<? super ItemStack> predicate, String message) {
    ItemStack icon = this.requireItem(slot);
    if (!Objects.requireNonNull(predicate, "predicate").test(icon)) {
      throw new AssertionError(Objects.requireNonNull(message, "message"));
    }
    return this;
  }

  /**
   * Asserts that the item at a named slot matches the supplied predicate.
   *
   * @param namedSlot named slot
   * @param predicate item predicate
   * @param message assertion message
   * @return this harness
   */
  public MenuTestHarness<S> assertItem(
      String namedSlot, Predicate<? super ItemStack> predicate, String message) {
    return this.assertItem(this.layout().slot(namedSlot), predicate, message);
  }

  /**
   * Asserts the lore of the item at a raw slot.
   *
   * @param slot raw slot
   * @param expected expected plain-text lore lines
   * @return this harness
   */
  public MenuTestHarness<S> assertLore(int slot, List<String> expected) {
    ItemStack icon = this.requireItem(slot);
    List<String> actual = plainLore(icon);
    if (!Objects.requireNonNull(expected, "expected").equals(actual)) {
      throw new AssertionError(
          "Expected slot " + slot + " lore " + expected + " but found " + actual);
    }
    return this;
  }

  /**
   * Asserts the lore of the item at a named slot.
   *
   * @param namedSlot named slot
   * @param expected expected plain-text lore lines
   * @return this harness
   */
  public MenuTestHarness<S> assertLore(String namedSlot, List<String> expected) {
    return this.assertLore(this.layout().slot(namedSlot), expected);
  }

  /**
   * Asserts that the harness session has terminated through a terminal command.
   *
   * @return this harness
   */
  public MenuTestHarness<S> assertTerminated() {
    if (!this.terminated) {
      throw new AssertionError("Expected the test harness session to be terminated");
    }
    return this;
  }

  /**
   * Asserts that the latest terminal command specifically requested close.
   *
   * @return this harness
   */
  public MenuTestHarness<S> assertClosed() {
    if (!this.closed) {
      throw new AssertionError("Expected the test harness session to be closed");
    }
    return this;
  }

  /**
   * Asserts the current state using a predicate.
   *
   * @param predicate state predicate
   * @param message assertion message
   * @return this harness
   */
  public MenuTestHarness<S> assertState(Predicate<? super S> predicate, String message) {
    Objects.requireNonNull(predicate, "predicate");
    if (!predicate.test(this.state)) {
      throw new AssertionError(Objects.requireNonNull(message, "message"));
    }
    return this;
  }

  /**
   * Asserts that the latest outcome contains a feedback signal.
   *
   * @param signal expected signal
   * @return this harness
   */
  public MenuTestHarness<S> assertFeedback(MenuFeedbackSignal signal) {
    MenuTestOutcome<S> outcome =
        this.lastOutcome().orElseThrow(() -> new AssertionError("No click outcome is available"));
    if (!outcome.feedbackSignals().contains(Objects.requireNonNull(signal, "signal"))) {
      throw new AssertionError("Expected feedback signal: " + signal.value());
    }
    return this;
  }

  /**
   * Completes a pending asynchronous operation with a successful result.
   *
   * @param key task key
   * @param result operation result
   * @param <R> result type
   * @return this harness
   */
  public <R> MenuTestHarness<S> completeAsync(MenuTaskKey key, R result) {
    this.ensureOpen();
    PendingAsync<S, R> pending = this.requirePendingAsync(key);
    S candidate =
        Objects.requireNonNull(
            pending.command.onSuccess().apply(this.state, pending.generation, result),
            "The asynchronous success transition returned null");
    this.pendingAsync.remove(key);
    this.publish(candidate);
    return this;
  }

  /**
   * Completes a pending asynchronous operation with a failure.
   *
   * @param key task key
   * @param failure failure cause
   * @return this harness
   */
  public MenuTestHarness<S> failAsync(MenuTaskKey key, Throwable failure) {
    this.ensureOpen();
    PendingAsync<S, ?> pending = this.requirePendingAsync(key);
    S candidate =
        Objects.requireNonNull(
            pending
                .command
                .onFailure()
                .apply(this.state, pending.generation, Objects.requireNonNull(failure, "failure")),
            "The asynchronous failure transition returned null");
    this.pendingAsync.remove(key);
    this.publish(candidate);
    return this;
  }

  /**
   * Runs the requested number of periodic-task ticks.
   *
   * @param ticks number of ticks to run
   * @return this harness
   */
  public MenuTestHarness<S> runTicks(long ticks) {
    if (ticks <= 0) {
      throw new IllegalArgumentException("ticks must be greater than zero: " + ticks);
    }
    this.ensureOpen();

    for (long tick = 1; tick <= ticks; tick++) {
      this.runSingleTick();
    }

    return this;
  }

  private void runSingleTick() {
    List<ActivePeriodic<S>> snapshot = List.copyOf(this.activePeriodics.values());
    long execution = 1L;

    for (ActivePeriodic<S> active : snapshot) {
      if (!this.activePeriodics.containsKey(active.key)) {
        continue;
      }

      MenuTickContext<S> context =
          new MenuTickContext<>(
              this.sessionId,
              this.viewer,
              this.state,
              this.revision,
              active.key,
              this.nextTaskGeneration(active.key),
              execution++);
      MenuTickResult<S> result;
      try {
        result = active.task.tick(context);
      } catch (Exception e) {
        throw new AssertionError("Periodic task threw an exception", e);
      }
      this.applyTickResult(active.key, result);
    }
  }

  private void applyTickResult(MenuTaskKey key, MenuTickResult<S> result) {
    S renderedState = result.resolveState(this.state);
    if (result.renderRequested()) {
      this.publish(renderedState);
    }
    if (result.stopRequested()) {
      this.activePeriodics.remove(key);
    }
  }

  private void ensureOpen() {
    if (this.terminated) {
      throw new IllegalStateException("The test harness session has already terminated");
    }
  }

  private ItemStack requireItem(int slot) {
    return this.item(slot)
        .orElseThrow(() -> new AssertionError("Expected slot " + slot + " to contain an item"));
  }

  private <R> PendingAsync<S, R> requirePendingAsync(MenuTaskKey key) {
    @SuppressWarnings("unchecked")
    PendingAsync<S, R> pending = (PendingAsync<S, R>) this.pendingAsync.get(key);
    if (pending == null) {
      throw new IllegalStateException("No pending asynchronous operation for key: " + key.value());
    }
    return pending;
  }

  private long nextTaskGeneration(MenuTaskKey key) {
    return this.taskGenerations.compute(
        Objects.requireNonNull(key, "key"),
        (ignored, current) -> current == null ? 1L : Math.incrementExact(current));
  }

  private <R> void applyAsyncStart(MenuAsyncCommand<S, R> command) {
    long generation = this.nextTaskGeneration(command.key());
    S candidate =
        Objects.requireNonNull(
            command.onStart().apply(this.state, generation),
            "The asynchronous start transition returned null");
    this.pendingAsync.put(command.key(), new PendingAsync<>(command, generation, candidate));
    this.publish(candidate);
  }

  private void publish(S candidateState) {
    S checkedState = Objects.requireNonNull(candidateState, "candidateState");
    MenuFrame<S> nextFrame =
        MenuRenderer.render(this.menu, this.viewer, checkedState, this.historyDepth, this.theme);
    this.state = checkedState;
    this.frame = nextFrame;
    this.revision = Math.incrementExact(this.revision);
  }

  private static String plainName(ItemStack icon) {
    if (!icon.hasItemMeta()) {
      return "";
    }
    ItemMeta meta = icon.getItemMeta();
    Component display = meta.displayName();
    return display == null ? "" : PlainTextComponentSerializer.plainText().serialize(display);
  }

  private static List<String> plainLore(ItemStack icon) {
    if (!icon.hasItemMeta()) {
      return List.of();
    }
    ItemMeta meta = icon.getItemMeta();
    List<Component> lore = meta.lore();
    if (lore == null) {
      return List.of();
    }
    PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
    List<String> result = new ArrayList<>(lore.size());
    for (Component line : lore) {
      result.add(serializer.serialize(line));
    }
    return List.copyOf(result);
  }

  /** Mutable, non-thread-safe builder for {@link MenuTestHarness}. */
  public static final class Builder<S> {

    private final Menu<S> menu;
    private final Player viewer;
    private final S initialState;
    private int historyDepth;
    private MenuTheme defaultTheme = MenuTheme.defaults();

    private Builder(Menu<S> menu, Player viewer, S initialState) {
      this.menu = Objects.requireNonNull(menu, "menu");
      this.viewer = Objects.requireNonNull(viewer, "viewer");
      this.initialState = Objects.requireNonNull(initialState, "initialState");
    }

    /**
     * Sets the navigation-history depth visible to the menu.
     *
     * @param historyDepth non-negative history depth
     * @return this builder
     */
    public Builder<S> historyDepth(int historyDepth) {
      if (historyDepth < 0) {
        throw new IllegalArgumentException("historyDepth must be >= 0: " + historyDepth);
      }
      this.historyDepth = historyDepth;
      return this;
    }

    /**
     * Sets the framework-level theme supplied to {@link Menu#theme(MenuTheme)}.
     *
     * @param defaultTheme default theme
     * @return this builder
     */
    public Builder<S> defaultTheme(MenuTheme defaultTheme) {
      this.defaultTheme = Objects.requireNonNull(defaultTheme, "defaultTheme");
      return this;
    }

    /**
     * Builds and initially renders the harness.
     *
     * @return initialized harness
     */
    public MenuTestHarness<S> build() {
      return new MenuTestHarness<>(this);
    }
  }
}
