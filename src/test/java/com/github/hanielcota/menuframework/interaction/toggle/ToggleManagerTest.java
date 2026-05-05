package com.github.hanielcota.menuframework.interaction.toggle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.hanielcota.menuframework.api.ClickContext;
import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.api.ToggleHandler;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.definition.ToggleState;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ToggleManager Tests")
class ToggleManagerTest {

  private final ToggleManager toggleManager = new ToggleManager();

  @Test
  @DisplayName("Should identify toggle slot")
  void shouldIdentifyToggleSlot() {
    var enabled = ItemTemplate.builder(Material.STONE).amount(1).build();
    var disabled = ItemTemplate.builder(Material.DIRT).amount(1).build();
    var slot = SlotDefinition.toggle(0, enabled, disabled, true, (ctx, state) -> {});

    assertTrue(toggleManager.isToggleSlot(slot));
  }

  @Test
  @DisplayName("Should not identify regular slot as toggle")
  void shouldNotIdentifyRegularSlotAsToggle() {
    var template = ItemTemplate.builder(Material.STONE).amount(1).build();
    var slot = SlotDefinition.of(0, template, null);

    assertFalse(toggleManager.isToggleSlot(slot));
  }

  @Test
  @DisplayName("Should toggle state on handle")
  void shouldToggleState() {
    var enabled = ItemTemplate.builder(Material.STONE).amount(1).build();
    var disabled = ItemTemplate.builder(Material.DIRT).amount(1).build();
    var state = new ToggleState(enabled, disabled, true);
    var session = mock(MenuSession.class);

    toggleManager.handleToggle(null, session, 0, ClickType.LEFT, null, state);

    assertFalse(state.isEnabled());
    verify(session).updateSlot(0, disabled);
  }

  @Test
  @DisplayName("Should invoke toggle handler safely")
  void shouldInvokeToggleHandlerSafely() {
    var handler = mock(ToggleHandler.class);
    var context = mock(ClickContext.class);

    toggleManager.invokeToggleHandlerSafely(handler, context, true, "menu", "uuid", 0);

    verify(handler).onToggle(context, true);
  }

  @Test
  @DisplayName("Should catch exception from toggle handler")
  void shouldCatchHandlerException() {
    var handler = mock(ToggleHandler.class);
    var context = mock(ClickContext.class);
    doThrow(new RuntimeException("Test")).when(handler).onToggle(any(), anyBoolean());

    assertDoesNotThrow(() ->
        toggleManager.invokeToggleHandlerSafely(handler, context, true, "menu", "uuid", 0));
  }

  @Test
  @DisplayName("Should toggle back to enabled")
  void shouldToggleBackToEnabled() {
    var enabled = ItemTemplate.builder(Material.STONE).amount(1).build();
    var disabled = ItemTemplate.builder(Material.DIRT).amount(1).build();
    var state = new ToggleState(enabled, disabled, false);
    var session = mock(MenuSession.class);

    toggleManager.handleToggle(null, session, 0, ClickType.LEFT, null, state);

    assertTrue(state.isEnabled());
    verify(session).updateSlot(0, enabled);
  }
}
