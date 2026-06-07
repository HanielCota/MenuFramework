package dev.haniel.menu.paper.placeholder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.placeholder.PlaceholderResolver;
import dev.haniel.menu.template.IconFactory;
import java.util.List;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class ResolvedIconFactoryTest {

  private final PlayerId viewer = new PlayerId(UUID.randomUUID());

  @Test
  void resolvesNameAndLoreForTheViewer() {
    Icon[] captured = new Icon[1];
    IconFactory<ItemStack> delegate = capture(captured);
    PlaceholderResolver resolver = (player, text) -> text.replace("%p%", "Steve");
    ResolvedIconFactory factory = new ResolvedIconFactory(delegate, resolver, viewer);

    factory.create(Icon.of("PAPER").named("Hi %p%").describedBy(List.of("Bye %p%")));

    assertEquals("Hi Steve", captured[0].name());
    assertEquals(List.of("Bye Steve"), captured[0].lore());
  }

  @Test
  void preservesTraitsWhileResolving() {
    Icon[] captured = new Icon[1];
    ResolvedIconFactory factory =
        new ResolvedIconFactory(capture(captured), (player, text) -> "x", viewer);

    factory.create(Icon.of("PAPER").named("%p%").glowing().amount(4));

    assertEquals(4, captured[0].traits().amount());
    assertEquals(true, captured[0].traits().glowing());
  }

  @Test
  void passesIconsWithoutPlaceholdersThroughUntouched() {
    Icon[] captured = new Icon[1];
    Icon original = Icon.of("PAPER").named("plain").describedBy(List.of("no tokens"));
    ResolvedIconFactory factory = new ResolvedIconFactory(capture(captured), failing(), viewer);

    factory.create(original);

    assertSame(original, captured[0], "an icon without a placeholder must not be reallocated");
  }

  private static IconFactory<ItemStack> capture(Icon[] captured) {
    return icon -> {
      captured[0] = icon;
      return null;
    };
  }

  private static PlaceholderResolver failing() {
    return (player, text) -> {
      throw new AssertionError("resolver must not run when there is no placeholder");
    };
  }
}
