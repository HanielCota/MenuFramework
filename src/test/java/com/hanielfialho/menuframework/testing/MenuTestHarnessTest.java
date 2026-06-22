package com.hanielfialho.menuframework.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.component.MenuButton;
import com.hanielfialho.menuframework.api.feedback.StandardMenuFeedbackSignals;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

final class MenuTestHarnessTest {

  private ServerMock server;
  private PlayerMock player;

  @BeforeEach
  void setUp() {
    this.server = MockBukkit.mock();
    this.player = this.server.addPlayer();
  }

  @AfterEach
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void harnessRendersNamedComponentSlotsAndCapturesFeedback() {
    MenuTestHarness<Boolean> harness =
        MenuTestHarness.create(new ToggleMenu(), this.player, Boolean.TRUE);

    harness.assertItem("toggle", Material.EMERALD).assertClickable("toggle").assertEmpty("hidden");

    MenuTestOutcome<Boolean> outcome = harness.click("toggle", ClickType.LEFT);

    assertFalse(outcome.state());
    assertTrue(outcome.rendered());
    assertEquals(List.of(StandardMenuFeedbackSignals.BUTTON_CLICK), outcome.feedbackSignals());
    harness.assertItem("toggle", Material.REDSTONE).assertNotClickable("toggle");
  }

  private static final class ToggleMenu implements Menu<Boolean> {

    private final MenuLayout layout =
        MenuLayout.chestBuilder(1).slot("toggle", 0).slot("hidden", 1).build();

    @Override
    public MenuLayout layout() {
      return this.layout;
    }

    @Override
    public Component title(@NonNull MenuRenderContext<Boolean> context) {
      return Component.text("Toggle");
    }

    @Override
    public void render(MenuRenderContext<Boolean> context, MenuCanvas<Boolean> canvas) {
      canvas.component(
          context,
          MenuButton.<Boolean>at("toggle")
              .icon(new ItemStack(Material.EMERALD))
              .disabledIcon(new ItemStack(Material.REDSTONE))
              .enabledWhen(MenuRenderContext::state)
              .onClick(interaction -> interaction.setState(Boolean.FALSE))
              .build());

      canvas.component(
          context,
          MenuButton.<Boolean>at("hidden")
              .icon(new ItemStack(Material.DIAMOND))
              .visibleWhen(ignored -> false)
              .onClick(interaction -> interaction.refresh())
              .build());
    }
  }
}
