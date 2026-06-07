package dev.haniel.menu.compiler.reader;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.item.MenuItem;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/** Centralizes annotation method signature validation for static and paginated readers. */
final class MethodSignatureValidator {

  void requirePaginatedProvider(Method method) {
    if (method.getParameterCount() != 0 || !returnsMenuItemList(method)) {
      throw new InvalidMenuException(
          "@Paginated method " + method.getName() + " must take no args and return List<MenuItem>");
    }
  }

  private boolean returnsMenuItemList(Method method) {
    if (method.getReturnType() != List.class) {
      return false;
    }
    return elementType(method.getGenericReturnType()) == MenuItem.class;
  }

  private Type elementType(Type returnType) {
    if (returnType instanceof ParameterizedType parameterized) {
      return parameterized.getActualTypeArguments()[0];
    }
    return Object.class;
  }
}
