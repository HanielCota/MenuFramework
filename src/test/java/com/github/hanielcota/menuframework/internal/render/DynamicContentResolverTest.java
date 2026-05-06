package com.github.hanielcota.menuframework.internal.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.hanielcota.menuframework.MenuFrameworkConfig;
import com.github.hanielcota.menuframework.api.MenuService;
import com.github.hanielcota.menuframework.api.MenuSession;
import com.github.hanielcota.menuframework.core.server.ServerAccess;
import com.github.hanielcota.menuframework.definition.ItemTemplate;
import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.internal.registry.DynamicContentRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Dynamic Content Resolver Tests")
class DynamicContentResolverTest {

  @Test
  @DisplayName("Should convert null provider result to empty content")
  void shouldConvertNullProviderResultToEmptyContent() {
    var fixture = fixture();
    when(fixture.registry().getDynamicContentProvider("menu"))
        .thenReturn(Optional.of((player, session) -> null));

    var result = fixture.resolver().resolve(fixture.view(), "menu");

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should ignore null provider entries")
  void shouldIgnoreNullProviderEntries() {
    var fixture = fixture();
    var slot = SlotDefinition.of(0, ItemTemplate.builder(Material.PAPER).build(), null);
    var items = new ArrayList<SlotDefinition>();
    items.add(null);
    items.add(slot);
    when(fixture.registry().getDynamicContentProvider("menu"))
        .thenReturn(Optional.of((player, session) -> items));

    var result = fixture.resolver().resolve(fixture.view(), "menu");

    assertEquals(List.of(slot), result);
  }

  private static Fixture fixture() {
    var registry = mock(DynamicContentRegistry.class);
    var serverAccess = mock(ServerAccess.class);
    var menuService = mock(MenuService.class);
    var view = mock(InventoryView.class);
    var player = mock(Player.class);
    var session = mock(MenuSession.class);
    var playerUuid = UUID.randomUUID();

    when(view.getPlayer()).thenReturn(player);
    when(player.getUniqueId()).thenReturn(playerUuid);
    when(serverAccess.findOnlinePlayer(playerUuid)).thenReturn(Optional.of(player));
    when(menuService.getSession(playerUuid)).thenReturn(Optional.of(session));

    var resolver =
        new DynamicContentResolver(
            registry, new SlowRenderLogger(new MenuFrameworkConfig()), serverAccess, menuService);
    return new Fixture(resolver, registry, view);
  }

  private record Fixture(
      DynamicContentResolver resolver, DynamicContentRegistry registry, InventoryView view) {}
}
