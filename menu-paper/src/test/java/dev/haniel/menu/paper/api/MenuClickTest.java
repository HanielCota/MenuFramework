package dev.haniel.menu.paper.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.paper.listener.PaperClickContext;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/** Probes the navigation contract of a {@link MenuClick} built without an opener. */
class MenuClickTest {

  private static final PaperClickContext CONTEXT =
      new PaperClickContext(new PlayerId(UUID.randomUUID()), ClickType.LEFT);

  @Test
  void openByIdThrowsWhenBuiltWithoutNavigation() {
    MenuClick click = MenuClick.of(CONTEXT);

    IllegalStateException error =
        assertThrows(IllegalStateException.class, () -> click.open(new MenuId("target")));
    assertTrue(error.getMessage().contains("Navigation is unavailable"));
  }

  @Test
  void openByClassThrowsWhenBuiltWithoutNavigation() {
    MenuClick click = MenuClick.of(CONTEXT);

    assertThrows(IllegalStateException.class, () -> click.open(Object.class));
  }

  @Test
  void soundPlaysTheGivenSoundToTheClickingPlayer() {
    UUID id = UUID.randomUUID();
    PaperClickContext context = new PaperClickContext(new PlayerId(id), ClickType.LEFT);
    Player player = mock(Player.class);
    Sound sound = Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f);

    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(() -> Bukkit.getPlayer(id)).thenReturn(player);

      new MenuClick(context, MiniMessage.miniMessage()).sound(sound);
    }

    verify(player).playSound(sound);
  }

  @Test
  void soundByKeyPlaysAMasterSoundAtFullVolume() {
    UUID id = UUID.randomUUID();
    PaperClickContext context = new PaperClickContext(new PlayerId(id), ClickType.LEFT);
    Player player = mock(Player.class);

    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(() -> Bukkit.getPlayer(id)).thenReturn(player);

      new MenuClick(context, MiniMessage.miniMessage()).sound("minecraft:ui.button.click");
    }

    verify(player)
        .playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1f, 1f));
  }

  @Test
  void promptThrowsWhenBuiltWithoutPrompts() {
    MenuClick click = MenuClick.of(CONTEXT);

    IllegalStateException error =
        assertThrows(IllegalStateException.class, () -> click.prompt(AnvilPrompt.text()));
    assertTrue(error.getMessage().contains("Prompts are unavailable"));
  }
}
