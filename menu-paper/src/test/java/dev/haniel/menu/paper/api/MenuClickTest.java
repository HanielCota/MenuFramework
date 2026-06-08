package dev.haniel.menu.paper.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.paper.listener.PaperClickContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;

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
  void promptThrowsWhenBuiltWithoutPrompts() {
    MenuClick click = MenuClick.of(CONTEXT);

    IllegalStateException error =
        assertThrows(IllegalStateException.class, () -> click.prompt(AnvilPrompt.text()));
    assertTrue(error.getMessage().contains("Prompts are unavailable"));
  }
}
