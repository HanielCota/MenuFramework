package dev.haniel.menu.compiler.reader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.item.MenuItem;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Adversarial signature cases for the paginated-provider validator: generic element-type variants
 * (wildcards, bounded type variables, nested lists), {@code ArrayList} concrete returns, and
 * boundary arg counts. The contract is "no args, returns {@code List<MenuItem>}".
 */
class MethodSignatureValidatorEdgeCasesTest {

  private final MethodSignatureValidator validator = new MethodSignatureValidator();

  @Test
  void rejectsBoundedWildcardElementType() {
    // List<? extends MenuItem> is NOT List<MenuItem>; the wildcard type arg is not MenuItem.class.
    assertThrows(
        InvalidMenuException.class,
        () -> validator.requirePaginatedProvider(method("extendsMenuItem")));
  }

  @Test
  void rejectsSuperWildcardElementType() {
    assertThrows(
        InvalidMenuException.class,
        () -> validator.requirePaginatedProvider(method("superMenuItem")));
  }

  @Test
  void rejectsUnboundedWildcardElementType() {
    assertThrows(
        InvalidMenuException.class,
        () -> validator.requirePaginatedProvider(method("unboundedWildcard")));
  }

  @Test
  void rejectsNestedListOfMenuItemLists() {
    assertThrows(
        InvalidMenuException.class, () -> validator.requirePaginatedProvider(method("nestedList")));
  }

  @Test
  void acceptsConcreteArrayListReturnType() {
    // ArrayList<MenuItem> is a valid List<MenuItem> implementation.
    assertDoesNotThrow(() -> validator.requirePaginatedProvider(method("arrayListReturn")));
  }

  @Test
  void rejectsTwoArgMethod() {
    assertThrows(
        InvalidMenuException.class,
        () -> validator.requirePaginatedProvider(method("twoArgs", int.class, int.class)));
  }

  @Test
  void rejectsVoidReturn() {
    assertThrows(
        InvalidMenuException.class,
        () -> validator.requirePaginatedProvider(method("returnsVoid")));
  }

  @Test
  void acceptsCanonicalNoArgListOfMenuItem() {
    assertDoesNotThrow(() -> validator.requirePaginatedProvider(method("canonical")));
  }

  private static java.lang.reflect.Method method(String name, Class<?>... parameters) {
    try {
      return Providers.class.getDeclaredMethod(name, parameters);
    } catch (NoSuchMethodException exception) {
      throw new IllegalStateException(exception);
    }
  }

  @SuppressWarnings("unused")
  static final class Providers {

    List<MenuItem> canonical() {
      return List.of();
    }

    List<? extends MenuItem> extendsMenuItem() {
      return List.of();
    }

    List<? super MenuItem> superMenuItem() {
      return new ArrayList<>();
    }

    List<?> unboundedWildcard() {
      return List.of();
    }

    List<List<MenuItem>> nestedList() {
      return List.of();
    }

    ArrayList<MenuItem> arrayListReturn() {
      return new ArrayList<>();
    }

    List<MenuItem> twoArgs(int a, int b) {
      return List.of();
    }

    void returnsVoid() {}
  }
}
