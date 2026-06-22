package com.hanielfialho.menuframework.internal.session;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuContext;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.feedback.MenuFeedback;
import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import com.hanielfialho.menuframework.api.theme.MenuTheme;
import com.hanielfialho.menuframework.internal.inventory.MenuHolder;
import com.hanielfialho.menuframework.internal.inventory.MenuViewAccess;
import com.hanielfialho.menuframework.internal.render.MenuFrame;
import com.hanielfialho.menuframework.internal.task.MenuTaskHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * Estado interno de uma única abertura de menu para um único jogador.
 *
 * <p>O estado visual é publicado como um snapshot atômico, evitando leituras inconsistentes de
 * {@code state}, {@code frame} e {@code revision}. As mutações continuam confinadas ao entity
 * scheduler do jogador. O registry de tasks possui lock próprio porque conclusões assíncronas e o
 * shutdown podem cancelá-las a partir de outras threads.
 */
public final class MenuSession<S> {

  private final UUID id;
  private final UUID viewerId;
  private final Menu<S> menu;
  private final MenuLayout layout;
  private final MenuHolder holder;
  private final int historyDepth;
  private final MenuTheme theme;
  private final MenuFeedback feedback;

  private final AtomicBoolean opened;
  private final AtomicBoolean lifecycleStarted;
  private final AtomicBoolean disposed;

  private final Object stateLock;
  private final Object taskLock;
  private final Map<MenuTaskKey, Long> taskGenerations;
  private final Map<MenuTaskKey, MenuTaskHandle> activeTasks;

  private volatile Snapshot<S> snapshot;

  public MenuSession(
      UUID id,
      UUID viewerId,
      Menu<S> menu,
      MenuHolder holder,
      S initialState,
      MenuFrame<S> initialFrame,
      int historyDepth,
      MenuTheme theme,
      MenuFeedback feedback) {
    this.id = Objects.requireNonNull(id, "id");
    this.viewerId = Objects.requireNonNull(viewerId, "viewerId");
    this.menu = Objects.requireNonNull(menu, "menu");
    this.holder = Objects.requireNonNull(holder, "holder");

    if (historyDepth < 0) {
      throw new IllegalArgumentException("historyDepth must be >= 0: " + historyDepth);
    }

    this.historyDepth = historyDepth;
    this.theme = Objects.requireNonNull(theme, "theme");
    this.feedback = Objects.requireNonNull(feedback, "feedback");

    S checkedInitialState = Objects.requireNonNull(initialState, "initialState");
    MenuFrame<S> checkedInitialFrame = Objects.requireNonNull(initialFrame, "initialFrame");

    this.layout = checkedInitialFrame.layout();
    this.snapshot = new Snapshot<>(checkedInitialState, checkedInitialFrame, 1L);

    this.opened = new AtomicBoolean();
    this.lifecycleStarted = new AtomicBoolean();
    this.disposed = new AtomicBoolean();

    this.stateLock = new Object();
    this.taskLock = new Object();
    this.taskGenerations = new HashMap<>();
    this.activeTasks = new HashMap<>();

    if (!holder.sessionId().equals(id)) {
      throw new IllegalArgumentException("Holder session id does not match the session id");
    }

    if (holder.getInventory().getSize() != this.layout.size()) {
      throw new IllegalArgumentException("Inventory size does not match the menu layout");
    }
  }

  public MenuSession(
      UUID id,
      UUID viewerId,
      Menu<S> menu,
      MenuHolder holder,
      S initialState,
      MenuFrame<S> initialFrame,
      int historyDepth) {
    this(
        id,
        viewerId,
        menu,
        holder,
        initialState,
        initialFrame,
        historyDepth,
        MenuTheme.defaults(),
        MenuFeedback.none());
  }

  public MenuSession(
      UUID id,
      UUID viewerId,
      Menu<S> menu,
      MenuHolder holder,
      S initialState,
      MenuFrame<S> initialFrame) {
    this(
        id,
        viewerId,
        menu,
        holder,
        initialState,
        initialFrame,
        0,
        MenuTheme.defaults(),
        MenuFeedback.none());
  }

  public UUID id() {
    return this.id;
  }

  public UUID viewerId() {
    return this.viewerId;
  }

  public Menu<S> menu() {
    return this.menu;
  }

  public MenuLayout layout() {
    return this.layout;
  }

  public int historyDepth() {
    return this.historyDepth;
  }

  public MenuTheme theme() {
    return this.theme;
  }

  public MenuFeedback feedback() {
    return this.feedback;
  }

  public Inventory inventory() {
    return this.holder.getInventory();
  }

  public S state() {
    return this.snapshot.state();
  }

  public MenuFrame<S> frame() {
    return this.snapshot.frame();
  }

  public long revision() {
    return this.snapshot.revision();
  }

  public boolean opened() {
    return this.opened.get();
  }

  public boolean lifecycleStarted() {
    return this.lifecycleStarted.get();
  }

  public boolean disposed() {
    return this.disposed.get();
  }

  public boolean markOpened() {
    return !this.disposed() && this.opened.compareAndSet(false, true);
  }

  /**
   * Reserva a execução de {@link Menu#onOpen} para esta sessão.
   *
   * <p>A mesma flag também define se {@link Menu#onClose} deverá ser executado. Assim, uma sessão
   * que falha antes do callback de abertura não recebe um callback de fechamento sem par.
   */
  public boolean claimOpenCallback() {
    return this.opened() && !this.disposed() && this.lifecycleStarted.compareAndSet(false, true);
  }

  public MenuContext<S> context(Player viewer) {
    Objects.requireNonNull(viewer, "viewer");

    if (!viewer.getUniqueId().equals(this.viewerId)) {
      throw new IllegalArgumentException("Viewer does not own this menu session");
    }

    Snapshot<S> current = this.snapshot;

    return new MenuContext<>(
        this.id, viewer, current.state(), current.revision(), this.historyDepth);
  }

  /**
   * Reserva uma geração sem substituir a task ativa da mesma chave.
   *
   * <p>A substituição só ocorre em {@link #activateTask(MenuTaskHandle)}. Esse protocolo em duas
   * fases evita cancelar uma task saudável quando a criação da substituta falha antes de ser
   * agendada.
   */
  public MenuTaskHandle reserveTask(MenuTaskKey key) {
    Objects.requireNonNull(key, "key");

    synchronized (this.taskLock) {
      if (this.disposed()) {
        throw new IllegalStateException("Cannot reserve a task for a disposed session");
      }

      long current = this.taskGenerations.getOrDefault(key, 0L);
      long next = Math.incrementExact(current);
      this.taskGenerations.put(key, next);
      return new MenuTaskHandle(key, next);
    }
  }

  /** Publica uma task reservada e cancela a geração ativa anterior. */
  public boolean activateTask(MenuTaskHandle handle) {
    Objects.requireNonNull(handle, "handle");

    MenuTaskHandle previous;

    synchronized (this.taskLock) {
      Long latestGeneration = this.taskGenerations.get(handle.key());

      if (this.disposed()
          || !handle.active()
          || latestGeneration == null
          || latestGeneration != handle.generation()) {
        return false;
      }

      previous = this.activeTasks.put(handle.key(), handle);
    }

    if (previous != null && previous != handle) {
      previous.cancel();
    }

    return true;
  }

  public boolean isTaskCurrent(MenuTaskHandle handle) {
    Objects.requireNonNull(handle, "handle");

    synchronized (this.taskLock) {
      return !this.disposed() && handle.active() && this.activeTasks.get(handle.key()) == handle;
    }
  }

  /**
   * Reserva a conclusão da task e a remove do registry.
   *
   * <p>Apenas o handle que ainda ocupa a chave pode concluir a transição.
   */
  public boolean claimTaskCompletion(MenuTaskHandle handle) {
    Objects.requireNonNull(handle, "handle");

    synchronized (this.taskLock) {
      if (this.disposed() || this.activeTasks.get(handle.key()) != handle) {
        return false;
      }

      this.activeTasks.remove(handle.key());
    }

    return handle.complete();
  }

  public boolean cancelTask(MenuTaskKey key) {
    Objects.requireNonNull(key, "key");

    MenuTaskHandle removed;

    synchronized (this.taskLock) {
      removed = this.activeTasks.remove(key);
    }

    if (removed == null) {
      return false;
    }

    removed.cancel();
    return true;
  }

  public boolean cancelTask(MenuTaskHandle handle) {
    Objects.requireNonNull(handle, "handle");

    boolean removed = false;

    synchronized (this.taskLock) {
      if (this.activeTasks.get(handle.key()) == handle) {
        this.activeTasks.remove(handle.key());
        removed = true;
      }
    }

    handle.cancel();
    return removed;
  }

  public void commit(S newState, MenuFrame<S> newFrame) {
    S checkedState = Objects.requireNonNull(newState, "newState");
    MenuFrame<S> checkedFrame = Objects.requireNonNull(newFrame, "newFrame");

    if (!this.layout.equals(checkedFrame.layout())) {
      throw new IllegalArgumentException("A menu cannot change its layout while open");
    }

    synchronized (this.stateLock) {
      if (this.disposed()) {
        throw new IllegalStateException("Cannot commit a disposed menu session");
      }

      Snapshot<S> current = this.snapshot;
      this.snapshot =
          new Snapshot<>(checkedState, checkedFrame, Math.incrementExact(current.revision()));
    }
  }

  /**
   * Descarte idempotente da sessão.
   *
   * <p>Toda task é removida do registry antes do cancelamento. Assim, mesmo que algum {@code
   * CompletionStage} ignore {@code cancel()}, seu callback não consegue voltar a ser considerado
   * atual.
   */
  public boolean dispose() {
    synchronized (this.stateLock) {
      if (!this.disposed.compareAndSet(false, true)) {
        return false;
      }
    }

    MenuViewAccess.untrack(this.holder);

    List<MenuTaskHandle> tasks;

    synchronized (this.taskLock) {
      tasks = new ArrayList<>(this.activeTasks.values());
      this.activeTasks.clear();
      this.taskGenerations.clear();
    }

    for (MenuTaskHandle task : tasks) {
      task.cancel();
    }

    return true;
  }

  private record Snapshot<S>(S state, MenuFrame<S> frame, long revision) {

    private Snapshot {
      Objects.requireNonNull(state, "state");
      Objects.requireNonNull(frame, "frame");

      if (revision <= 0L) {
        throw new IllegalArgumentException("revision must be greater than zero: " + revision);
      }
    }
  }
}
