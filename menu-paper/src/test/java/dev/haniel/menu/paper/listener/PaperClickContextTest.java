package dev.haniel.menu.paper.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.PlayerId;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class PaperClickContextTest {

  @Test
  void exposesPlayerClickTypeAndEntity() {
    PlayerId id = new PlayerId(UUID.randomUUID());
    Player entity = mock(Player.class);
    PaperClickContext context = new PaperClickContext(id, ClickType.SHIFT_LEFT, entity);

    assertEquals(id, context.player());
    assertEquals(ClickType.SHIFT_LEFT, context.clickType());
    assertSame(entity, context.playerEntity());
  }

  @Test
  void honoursTheClickContextContract() {
    PlayerId id = new PlayerId(UUID.randomUUID());
    ClickContext context = new PaperClickContext(id, ClickType.RIGHT, mock(Player.class));

    assertEquals(id, context.player());
    assertEquals(ClickType.RIGHT, context.clickType());
  }

  @Test
  void equalsByItsComponents() {
    UUID uuid = UUID.randomUUID();
    Player entity = mock(Player.class);
    PaperClickContext first = new PaperClickContext(new PlayerId(uuid), ClickType.LEFT, entity);
    PaperClickContext second = new PaperClickContext(new PlayerId(uuid), ClickType.LEFT, entity);

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }
}
