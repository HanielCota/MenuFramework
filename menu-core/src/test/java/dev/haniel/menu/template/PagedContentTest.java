package dev.haniel.menu.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import dev.haniel.menu.compiler.binding.ContentProvider;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.junit.jupiter.api.Test;

class PagedContentTest {

  private static ContentProvider providerYielding(List<MenuItem> items) {
    MethodHandle handle = MethodHandles.constant(Object.class, items);
    return new ContentProvider(handle);
  }

  @Test
  void itemsDelegatesToTheProvider() {
    List<MenuItem> items = List.of(MenuItem.of(Icon.of("STONE")));
    PagedContent<String> content = new PagedContent<>(providerYielding(items), icon -> "v");

    assertEquals(items, content.items());
  }

  @Test
  void renderRunsTheFactoryOnTheItemIcon() {
    PagedContent<String> content = new PagedContent<>(providerYielding(List.of()), Icon::material);
    MenuItem item = MenuItem.of(Icon.of("DIAMOND"));

    assertEquals("DIAMOND", content.render(item));
  }

  @Test
  void renderPassesTheItemsOwnIconToTheFactory() {
    Icon icon = Icon.of("EMERALD");
    Icon[] seen = new Icon[1];
    PagedContent<String> content =
        new PagedContent<>(
            providerYielding(List.of()),
            received -> {
              seen[0] = received;
              return "v";
            });

    content.render(MenuItem.of(icon));

    assertSame(icon, seen[0]);
  }
}
