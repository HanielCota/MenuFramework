package com.hanielfialho.menuframework.internal.render;

import com.hanielfialho.menuframework.internal.inventory.MenuViewAccess;
import com.hanielfialho.menuframework.internal.runtime.MenuRuntimeState;
import com.hanielfialho.menuframework.internal.session.MenuSession;
import com.hanielfialho.menuframework.internal.session.MenuSessionRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Renders complete frames and applies only the required visual changes.
 *
 * <p>The differential application uses the inventory's actual contents rather than only the
 * previous frame. A refresh therefore repairs a slot changed by another plugin or by an unexpected
 * server-side mutation. If a normal runtime exception occurs while applying a frame or publishing
 * the session snapshot, already modified slots are restored in reverse order.
 */
public final class MenuFrameApplier {

  private final MenuRuntimeState runtimeState;
  private final MenuSessionRegistry sessions;

  public MenuFrameApplier(MenuRuntimeState runtimeState, MenuSessionRegistry sessions) {
    this.runtimeState = Objects.requireNonNull(runtimeState, "runtimeState");
    this.sessions = Objects.requireNonNull(sessions, "sessions");
  }

  public <S> void applyInitialFrame(Inventory inventory, MenuFrame<S> frame) {
    Objects.requireNonNull(inventory, "inventory");
    Objects.requireNonNull(frame, "frame");

    if (inventory.getSize() != frame.size()) {
      throw new IllegalArgumentException(
          "Inventory size must match the frame size: "
              + inventory.getSize()
              + " != "
              + frame.size());
    }

    ItemStack[] contents = new ItemStack[frame.size()];

    for (int slot = 0; slot < frame.size(); slot++) {
      MenuSlot<S> menuSlot = frame.slotOrNull(slot);

      if (menuSlot != null) {
        contents[slot] = menuSlot.icon();
      }
    }

    inventory.setContents(contents);
  }

  public <S> boolean renderAndApply(MenuSession<S> session, Player viewer, S candidateState) {
    Objects.requireNonNull(session, "session");
    Objects.requireNonNull(viewer, "viewer");
    Objects.requireNonNull(candidateState, "candidateState");

    if (!this.isUsable(session, viewer)) {
      return false;
    }

    MenuFrame<S> nextFrame =
        MenuRenderer.render(
            session.menu(), viewer, candidateState, session.historyDepth(), session.theme());

    if (!session.layout().equals(nextFrame.layout())) {
      throw new IllegalStateException("A menu cannot change its layout while open");
    }

    if (!this.isUsable(session, viewer)) {
      return false;
    }

    Inventory inventory = session.inventory();
    List<VisualChange> changes = this.calculateChanges(inventory, nextFrame);

    int appliedChanges = 0;

    try {
      for (VisualChange change : changes) {
        inventory.setItem(change.slot(), cloneOrNull(change.nextItem()));
        appliedChanges++;
      }

      if (!this.isUsable(session, viewer)) {
        RuntimeException rollbackFailure =
            this.rollbackStaleApplication(inventory, changes, appliedChanges);
        appliedChanges = 0;

        if (rollbackFailure != null) {
          throw rollbackFailure;
        }

        return false;
      }

      session.commit(candidateState, nextFrame);
      return true;
    } catch (RuntimeException failure) {
      this.rollback(inventory, changes, appliedChanges, failure);
      throw failure;
    }
  }

  private boolean isUsable(MenuSession<?> session, Player viewer) {
    return this.runtimeState.running()
        && this.sessions.isCurrent(session)
        && MenuViewAccess.isSessionInventoryOpen(viewer, session.id());
  }

  private RuntimeException rollbackStaleApplication(
      Inventory inventory, List<VisualChange> changes, int appliedChanges) {
    IllegalStateException rollbackMarker =
        new IllegalStateException("Failed to roll back a stale menu frame application");

    this.rollback(inventory, changes, appliedChanges, rollbackMarker);

    return rollbackMarker.getSuppressed().length == 0 ? null : rollbackMarker;
  }

  private <S> List<VisualChange> calculateChanges(Inventory inventory, MenuFrame<S> next) {
    List<VisualChange> changes = new ArrayList<>();

    for (int slot = 0; slot < next.size(); slot++) {
      ItemStack currentItem = snapshotOrNull(inventory.getItem(slot));
      MenuSlot<S> nextSlot = next.slotOrNull(slot);
      ItemStack nextItem = nextSlot == null ? null : nextSlot.icon();

      if (Objects.equals(currentItem, nextItem)) {
        continue;
      }

      changes.add(new VisualChange(slot, currentItem, nextItem));
    }

    return List.copyOf(changes);
  }

  private void rollback(
      Inventory inventory,
      List<VisualChange> changes,
      int appliedChanges,
      RuntimeException originalFailure) {
    for (int index = appliedChanges - 1; index >= 0; index--) {
      VisualChange change = changes.get(index);

      try {
        inventory.setItem(change.slot(), cloneOrNull(change.previousItem()));
      } catch (RuntimeException rollbackFailure) {
        originalFailure.addSuppressed(rollbackFailure);
      }
    }
  }

  private static ItemStack snapshotOrNull(ItemStack item) {
    if (item == null || item.getType().isAir()) {
      return null;
    }

    return item.clone();
  }

  private static ItemStack cloneOrNull(ItemStack item) {
    return item == null ? null : item.clone();
  }

  private record VisualChange(int slot, ItemStack previousItem, ItemStack nextItem) {}
}
