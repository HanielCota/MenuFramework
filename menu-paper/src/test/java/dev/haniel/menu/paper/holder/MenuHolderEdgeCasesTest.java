package dev.haniel.menu.paper.holder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.template.MenuTemplate;
import dev.haniel.menu.template.SlotBinding;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

/**
 * Adversarial probes of raw-slot routing in {@code MenuHolder.click}, exercised through the {@link
 * MenuTemplate} it delegates to (the holder's inventory build needs a live server, so we test the
 * routing seam directly via the template).
 *
 * <p>Hunts for: an action firing on an empty/unbound slot, an out-of-bounds raw slot triggering an
 * NPE or the wrong action, and the bottom-inventory click (raw slot &gt;= size) resolving to no
 * action.
 */
class MenuHolderEdgeCasesTest {

  @Test
  void unboundSlotFiresNoActionAndDoesNotThrow() {
    Recording bound = recording();
    MenuTemplate<ItemStack> template = template(9, new SlotBinding(3, bound));

    assertDoesNotThrow(() -> template.actionAt(0).ifPresent(action -> action.onClick(context())));
    assertNull(bound.lastContext);
  }

  @Test
  void boundSlotFiresExactlyThatAction() {
    Recording bound = recording();
    MenuTemplate<ItemStack> template = template(9, new SlotBinding(3, bound));
    ClickContext context = context();

    template.actionAt(3).ifPresent(action -> action.onClick(context));

    assertEquals(context, bound.lastContext);
  }

  /** A raw slot beyond the menu size (a bottom-inventory click) must resolve to no action. */
  @Test
  void rawSlotAtOrAboveSizeResolvesToNoAction() {
    Recording bound = recording();
    MenuTemplate<ItemStack> template = template(9, new SlotBinding(0, bound));

    template.actionAt(9).ifPresent(action -> action.onClick(context()));
    template.actionAt(53).ifPresent(action -> action.onClick(context()));

    assertNull(bound.lastContext);
  }

  /** A negative raw slot (click outside the window) must resolve to no action, never throw. */
  @Test
  void negativeRawSlotResolvesToNoAction() {
    Recording bound = recording();
    MenuTemplate<ItemStack> template = template(9, new SlotBinding(0, bound));

    assertDoesNotThrow(
        () -> template.actionAt(-999).ifPresent(action -> action.onClick(context())));
    assertNull(bound.lastContext);
  }

  private MenuTemplate<ItemStack> template(int size, SlotBinding... bindings) {
    return new MenuTemplate<>(new ItemStack[size], bindings);
  }

  private static Recording recording() {
    return new Recording();
  }

  private static ClickContext context() {
    return mock(ClickContext.class);
  }

  private static final class Recording implements MenuAction {
    ClickContext lastContext;

    @Override
    public void onClick(ClickContext context) {
      this.lastContext = context;
    }
  }
}
