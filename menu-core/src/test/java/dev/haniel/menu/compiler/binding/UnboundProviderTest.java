package dev.haniel.menu.compiler.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class UnboundProviderTest {

  @Test
  void bindsToInstanceAndProvidesItsItems() throws ReflectiveOperationException {
    UnboundProvider provider = new UnboundProvider(unboundItems());

    ContentProvider bound = provider.bind(new Source(1));

    assertEquals(1, bound.provide().size());
  }

  @Test
  void bindsEachInstanceIndependently() throws ReflectiveOperationException {
    UnboundProvider provider = new UnboundProvider(unboundItems());

    ContentProvider first = provider.bind(new Source(2));
    ContentProvider second = provider.bind(new Source(3));

    assertNotSame(first, second);
    assertEquals(2, first.provide().size());
    assertEquals(3, second.provide().size());
  }

  private static MethodHandle unboundItems() throws ReflectiveOperationException {
    return MethodHandles.lookup().unreflect(Source.class.getDeclaredMethod("items"));
  }

  static final class Source {

    private final int count;

    Source(int count) {
      this.count = count;
    }

    List<MenuItem> items() {
      return IntStream.range(0, count)
          .mapToObj(index -> MenuItem.of(Icon.of("STONE")))
          .toList();
    }
  }
}
