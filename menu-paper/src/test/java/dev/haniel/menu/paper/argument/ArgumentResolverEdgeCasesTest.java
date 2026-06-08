package dev.haniel.menu.paper.argument;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import dev.haniel.menu.click.ClickContext;
import dev.haniel.menu.click.ClickType;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.paper.api.MenuClick;
import dev.haniel.menu.paper.api.MenuOpener;
import dev.haniel.menu.paper.listener.PaperClickContext;
import java.util.UUID;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Adversarial probes of the Paper argument resolvers.
 *
 * <p>Hunts for: a resolver returning the wrong object (the id instead of the entity), matching a
 * type it must not (a supertype of {@link Player}), or silently producing garbage when handed a
 * non-Paper {@link ClickContext} from another platform.
 */
class ArgumentResolverEdgeCasesTest {

  private final PlayerArgumentResolver playerResolver = new PlayerArgumentResolver();
  private final MenuClickArgumentResolver clickResolver =
      new MenuClickArgumentResolver(
          MiniMessage.miniMessage(), noOpOpener(), (viewer, prompt) -> {});

  // ---- PlayerArgumentResolver ----

  @Test
  void playerResolverReturnsTheClickingEntityNotTheId() {
    Player player = mock(Player.class);
    UUID uuid = UUID.randomUUID();
    org.mockito.Mockito.when(player.getUniqueId()).thenReturn(uuid);
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(player);
      PaperClickContext context = new PaperClickContext(new PlayerId(uuid), ClickType.LEFT);

      Object resolved = playerResolver.resolve(context);

      assertSame(player, resolved);
    }
  }

  @Test
  void playerResolverSupportsExactPlayerType() {
    assertTrue(playerResolver.supports(Player.class));
  }

  /**
   * A {@code @Button} method declaring a supertype of {@link Player} (e.g. {@link HumanEntity} or
   * {@link CommandSender}) must NOT be matched, since the resolver hands back a {@link Player}; a
   * loose {@code isAssignableFrom} match would let a different resolver lose the slot.
   */
  @Test
  void playerResolverRejectsSupertypesOfPlayer() {
    assertFalse(playerResolver.supports(HumanEntity.class));
    assertFalse(playerResolver.supports(CommandSender.class));
    assertFalse(playerResolver.supports(Object.class));
  }

  /** A non-Paper context (cross-platform misuse) must fail loudly, not return null or garbage. */
  @Test
  void playerResolverThrowsOnNonPaperContext() {
    ClickContext foreign = foreignContext();

    assertThrows(IllegalStateException.class, () -> playerResolver.resolve(foreign));
  }

  // ---- MenuClickArgumentResolver ----

  @Test
  void clickResolverSupportsExactMenuClickType() {
    assertTrue(clickResolver.supports(MenuClick.class));
  }

  @Test
  void clickResolverRejectsUnrelatedTypes() {
    assertFalse(clickResolver.supports(Player.class));
    assertFalse(clickResolver.supports(ClickContext.class));
    assertFalse(clickResolver.supports(Object.class));
  }

  @Test
  void clickResolverProducesMenuClickOverTheClickingPlayer() {
    Player player = mock(Player.class);
    UUID uuid = UUID.randomUUID();
    org.mockito.Mockito.when(player.getUniqueId()).thenReturn(uuid);
    try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
      bukkit.when(() -> Bukkit.getPlayer(uuid)).thenReturn(player);

      Object resolved =
          clickResolver.resolve(new PaperClickContext(new PlayerId(uuid), ClickType.LEFT));

      assertTrue(resolved instanceof MenuClick);
      assertSame(player, ((MenuClick) resolved).player());
    }
  }

  @Test
  void clickResolverThrowsOnNonPaperContext() {
    ClickContext foreign = foreignContext();

    assertThrows(IllegalStateException.class, () -> clickResolver.resolve(foreign));
  }

  private static PaperClickContext paperContext(Player player) {
    UUID uuid = UUID.randomUUID();
    org.mockito.Mockito.when(player.getUniqueId()).thenReturn(uuid);
    return new PaperClickContext(new PlayerId(uuid), ClickType.LEFT);
  }

  private static MenuOpener noOpOpener() {
    return new MenuOpener() {
      @Override
      public void open(Player viewer, MenuId id) {}

      @Override
      public void open(Player viewer, Class<?> menuType) {}
    };
  }

  private static ClickContext foreignContext() {
    return new ClickContext() {
      @Override
      public PlayerId player() {
        return new PlayerId(UUID.randomUUID());
      }

      @Override
      public ClickType clickType() {
        return ClickType.LEFT;
      }
    };
  }
}
