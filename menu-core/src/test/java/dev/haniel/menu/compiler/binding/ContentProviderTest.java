package dev.haniel.menu.compiler.binding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.action.MenuActionException;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContentProviderTest {

  @Test
  void invokesHandleAndReturnsItems() throws ReflectiveOperationException {
    ContentProvider provider = new ContentProvider(boundTo(new Source(), "items"));

    assertEquals(2, provider.provide().size());
  }

  @Test
  void wrapsThrowingProviderInMenuActionException() throws ReflectiveOperationException {
    ContentProvider provider = new ContentProvider(boundTo(new Source(), "explode"));

    MenuActionException error = assertThrows(MenuActionException.class, provider::provide);
    assertInstanceOf(IllegalStateException.class, error.getCause());
  }

  @Test
  void failsWhenProviderReturnsNull() throws ReflectiveOperationException {
    ContentProvider provider = new ContentProvider(boundTo(new Source(), "missing"));

    MenuActionException error = assertThrows(MenuActionException.class, provider::provide);
    assertInstanceOf(NullPointerException.class, error.getCause());
  }

  private static MethodHandle boundTo(Source source, String method)
      throws ReflectiveOperationException {
    return MethodHandles.lookup().unreflect(Source.class.getDeclaredMethod(method)).bindTo(source);
  }

  static final class Source {

    List<MenuItem> items() {
      return List.of(MenuItem.of(Icon.of("STONE")), MenuItem.of(Icon.of("DIRT")));
    }

    List<MenuItem> explode() {
      throw new IllegalStateException("boom");
    }

    List<MenuItem> missing() {
      return null;
    }
  }
}
