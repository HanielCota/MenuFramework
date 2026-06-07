package dev.haniel.menu.compiler.reader;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.Paginated;
import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.model.PagedStructure;
import dev.haniel.menu.item.MenuItem;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import org.junit.jupiter.api.Test;

class PagedReaderTest {

  @Test
  void reusesPagedMetadataAcrossReadsWithCustomInstantiators() throws Throwable {
    PagedReader reader = new PagedReader();
    Instantiator first = instantiator("first");
    Instantiator second = instantiator("second");

    PagedStructure firstRead = reader.read(CustomInstantiatedMenu.class, first);
    PagedStructure secondRead = reader.read(CustomInstantiatedMenu.class, second);

    assertTrue(reader.handles(CustomInstantiatedMenu.class));
    assertSame(firstRead.provider(), secondRead.provider());
    assertSame(firstRead.buttons(), secondRead.buttons());
    assertSame(firstRead.states(), secondRead.states());
  }

  private Instantiator instantiator(String name) throws NoSuchMethodException, IllegalAccessException {
    return new Instantiator(
        MethodHandles.lookup()
            .findConstructor(CustomInstantiatedMenu.class, MethodType.methodType(void.class, String.class))
            .bindTo(name));
  }

  @Menu(id = "custom")
  static final class CustomInstantiatedMenu {

    @SuppressWarnings("unused")
    private final String name;

    CustomInstantiatedMenu(String name) {
      this.name = name;
    }

    @Paginated
    List<MenuItem> items() {
      return List.of();
    }

    @Button(id = "close")
    void close() {}
  }
}
