package dev.haniel.menu.paper.holder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.template.MenuTemplate;
import dev.haniel.menu.template.SlotBinding;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Probes that a {@code @Visible}-hidden slot is neither rendered nor clickable in a static menu.
 */
class MenuHolderVisibilityTest {

  @Test
  void hiddenSlotIsNotRenderedAndItsActionIsSkipped() {
    Recording action = new Recording();
    MenuTemplate<ItemStack> template = templateWithButton(action);
    Inventory inventory = inventory();
    when(inventory.getItem(3)).thenReturn(null); // hidden -> empty slot

    try (MockedStatic<Bukkit> bukkit = mockStaticInventory(inventory)) {
      MenuHolder holder = new MenuHolder(new MenuId("m"), template, Component.text("t"), Set.of(3));
      holder.click(3, mock(ClickContext.class));
    }

    verify(inventory, never()).setItem(eq(3), any());
    assertNull(action.lastContext, "a hidden button must not fire its action");
  }

  @Test
  void shownSlotIsRenderedAndClickable() {
    Recording action = new Recording();
    MenuTemplate<ItemStack> template = templateWithButton(action);
    Inventory inventory = inventory();
    when(inventory.getItem(3)).thenReturn(mock(ItemStack.class)); // rendered

    try (MockedStatic<Bukkit> bukkit = mockStaticInventory(inventory)) {
      MenuHolder holder = new MenuHolder(new MenuId("m"), template, Component.text("t"), Set.of());
      holder.click(3, mock(ClickContext.class));
    }

    verify(inventory).setItem(eq(3), any());
    assertNotNull(action.lastContext, "a shown button fires its action");
  }

  private MenuTemplate<ItemStack> templateWithButton(MenuAction action) {
    ItemStack[] visuals = new ItemStack[9];
    visuals[3] = mock(ItemStack.class);
    return new MenuTemplate<>(visuals, new SlotBinding[] {new SlotBinding(3, action)});
  }

  private Inventory inventory() {
    Inventory inventory = mock(Inventory.class);
    when(inventory.getSize()).thenReturn(9);
    return inventory;
  }

  private MockedStatic<Bukkit> mockStaticInventory(Inventory inventory) {
    MockedStatic<Bukkit> bukkit = org.mockito.Mockito.mockStatic(Bukkit.class);
    bukkit
        .when(() -> Bukkit.createInventory(any(), eq(9), any(Component.class)))
        .thenReturn(inventory);
    return bukkit;
  }

  private static final class Recording implements MenuAction {
    ClickContext lastContext;

    @Override
    public void onClick(ClickContext context) {
      this.lastContext = context;
    }
  }
}
