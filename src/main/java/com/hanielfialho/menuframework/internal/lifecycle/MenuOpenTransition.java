package com.hanielfialho.menuframework.internal.lifecycle;

import com.hanielfialho.menuframework.api.MenuCloseReason;
import com.hanielfialho.menuframework.internal.session.MenuSession;
import java.util.Objects;
import java.util.UUID;

/**
 * Alteração transacional da pilha de navegação associada a uma abertura.
 *
 * <p>A mutação é confirmada somente depois que a nova InventoryView foi aberta e validada. Dessa
 * forma, uma abertura cancelada preserva o histórico anterior.
 */
public final class MenuOpenTransition {

  private final Type type;
  private final MenuSession<?> source;
  private final MenuHistoryEntry<?> entry;
  private final int sourceDepth;
  private final int targetDepth;
  private final MenuCloseReason previousCloseReason;

  private MenuOpenTransition(
      Type type,
      MenuSession<?> source,
      MenuHistoryEntry<?> entry,
      int sourceDepth,
      int targetDepth,
      MenuCloseReason previousCloseReason) {
    this.type = Objects.requireNonNull(type, "type");
    this.source = source;
    this.entry = entry;
    this.sourceDepth = sourceDepth;
    this.targetDepth = targetDepth;
    this.previousCloseReason = Objects.requireNonNull(previousCloseReason, "previousCloseReason");
  }

  public static MenuOpenTransition root() {
    return new MenuOpenTransition(Type.ROOT, null, null, 0, 0, MenuCloseReason.REPLACED);
  }

  public static MenuOpenTransition forward(MenuSession<?> source, MenuHistoryRegistry history) {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(history, "history");

    int sourceDepth = verifiedSourceDepth(source, history);
    int targetDepth = Math.min(history.maxDepth(), Math.incrementExact(sourceDepth));

    return new MenuOpenTransition(
        Type.FORWARD,
        source,
        snapshot(source),
        sourceDepth,
        targetDepth,
        MenuCloseReason.NAVIGATION);
  }

  public static MenuOpenTransition back(MenuSession<?> source, MenuHistoryRegistry history) {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(history, "history");

    int sourceDepth = verifiedSourceDepth(source, history);

    if (sourceDepth == 0) {
      throw new IllegalStateException("The current menu has no previous history entry");
    }

    MenuHistoryEntry<?> target = history.peek(source.viewerId());

    if (target == null) {
      throw new IllegalStateException("The menu history depth is non-zero but has no top entry");
    }

    return new MenuOpenTransition(
        Type.BACK, source, target, sourceDepth, sourceDepth - 1, MenuCloseReason.BACK);
  }

  private static int verifiedSourceDepth(MenuSession<?> source, MenuHistoryRegistry history) {
    int registeredDepth = history.depth(source.viewerId());

    if (source.historyDepth() != registeredDepth) {
      throw new IllegalStateException(
          "Menu session history depth does not match the "
              + "registered history: "
              + source.historyDepth()
              + " != "
              + registeredDepth);
    }

    return registeredDepth;
  }

  private static MenuHistoryEntry<?> snapshot(MenuSession<?> source) {
    return snapshotTyped(source);
  }

  private static <S> MenuHistoryEntry<S> snapshotTyped(MenuSession<S> source) {
    return new MenuHistoryEntry<>(source.menu(), source.state());
  }

  public int targetDepth() {
    return this.targetDepth;
  }

  public MenuCloseReason previousCloseReason() {
    return this.previousCloseReason;
  }

  public MenuHistoryEntry<?> backTarget() {
    if (this.type != Type.BACK) {
      throw new IllegalStateException("Only a BACK transition has a history target");
    }

    return this.entry;
  }

  public boolean canCommit(MenuSession<?> current, MenuHistoryRegistry history, UUID viewerId) {
    Objects.requireNonNull(history, "history");
    Objects.requireNonNull(viewerId, "viewerId");

    if (this.type == Type.ROOT) {
      return true;
    }

    if (current != this.source
        || this.source == null
        || this.source.disposed()
        || !this.source.opened()
        || !viewerId.equals(this.source.viewerId())
        || this.source.historyDepth() != this.sourceDepth
        || history.depth(viewerId) != this.sourceDepth) {
      return false;
    }

    return this.type != Type.BACK || history.topIs(viewerId, this.entry);
  }

  public boolean commit(MenuHistoryRegistry history, UUID viewerId) {
    Objects.requireNonNull(history, "history");
    Objects.requireNonNull(viewerId, "viewerId");

    return switch (this.type) {
      case ROOT -> {
        history.clear(viewerId);
        yield history.depth(viewerId) == this.targetDepth;
      }

      case FORWARD -> history.push(viewerId, this.entry) == this.targetDepth;

      case BACK ->
          history.popIfTop(viewerId, this.entry) && history.depth(viewerId) == this.targetDepth;
    };
  }

  private enum Type {
    ROOT,
    FORWARD,
    BACK
  }
}
