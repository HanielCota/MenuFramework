package com.hanielfialho.menuframework.example.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hanielfialho.menuframework.testing.MenuTestHarness;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class CounterMenuTest {

  @Test
  void incrementsOnClick() {
    ServerMock server = MockBukkit.mock();
    PlayerMock player = server.addPlayer();

    try {
      CounterMenu menu = new CounterMenu(new SynchronousProductMenu(java.util.List.of()));
      MenuTestHarness<CounterMenu.State> harness =
          MenuTestHarness.create(menu, player, new CounterMenu.State(0));

      harness
          .assertItem("counter", Material.EMERALD)
          .assertClickable("counter")
          .click("counter", ClickType.LEFT);

      assertEquals(1, harness.state().clicks());
      assertTrue(harness.item("counter").isPresent());
    } finally {
      MockBukkit.unmock();
    }
  }

  @Test
  void closesOnCloseButton() {
    ServerMock server = MockBukkit.mock();
    PlayerMock player = server.addPlayer();

    try {
      CounterMenu menu = new CounterMenu(new SynchronousProductMenu(java.util.List.of()));
      MenuTestHarness<CounterMenu.State> harness =
          MenuTestHarness.create(menu, player, new CounterMenu.State(0));

      harness.click("close", ClickType.LEFT);

      assertTrue(harness.closed());
    } finally {
      MockBukkit.unmock();
    }
  }
}
