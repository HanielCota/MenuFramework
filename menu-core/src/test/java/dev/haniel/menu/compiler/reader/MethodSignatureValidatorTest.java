package dev.haniel.menu.compiler.reader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.item.MenuItem;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;

class MethodSignatureValidatorTest {

  private final MethodSignatureValidator validator = new MethodSignatureValidator();

  @Test
  void acceptsNoArgMethodReturningMenuItemList() {
    assertDoesNotThrow(() -> validator.requirePaginatedProvider(method("validItems")));
  }

  @Test
  void rejectsMethodWithParameters() {
    InvalidMenuException error =
        assertThrows(
            InvalidMenuException.class,
            () -> validator.requirePaginatedProvider(method("withArgs", int.class)));
    assertTrue(error.getMessage().contains("List<MenuItem>"));
  }

  @Test
  void rejectsMethodWithWrongRawReturnType() {
    assertThrows(
        InvalidMenuException.class,
        () -> validator.requirePaginatedProvider(method("returnsString")));
  }

  @Test
  void rejectsListOfWrongElementType() {
    assertThrows(
        InvalidMenuException.class,
        () -> validator.requirePaginatedProvider(method("returnsStringList")));
  }

  @Test
  void rejectsRawListReturnType() {
    assertThrows(
        InvalidMenuException.class, () -> validator.requirePaginatedProvider(method("returnsRawList")));
  }

  private static Method method(String name, Class<?>... parameters) {
    try {
      return Providers.class.getDeclaredMethod(name, parameters);
    } catch (NoSuchMethodException exception) {
      throw new IllegalStateException(exception);
    }
  }

  @SuppressWarnings({"unused", "rawtypes"})
  static final class Providers {

    List<MenuItem> validItems() {
      return List.of();
    }

    List<MenuItem> withArgs(int page) {
      return List.of();
    }

    String returnsString() {
      return "";
    }

    List<String> returnsStringList() {
      return List.of();
    }

    List returnsRawList() {
      return List.of();
    }
  }
}
