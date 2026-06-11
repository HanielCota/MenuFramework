package dev.haniel.menu.compiler.reader;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.domain.Page;
import dev.haniel.menu.item.MenuItem;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/** Centralizes annotation method signature validation for static and paginated readers. */
final class MethodSignatureValidator {

  void requirePaginatedProvider(Method method) {
    if (!isEagerProvider(method) && !isLazyProvider(method)) {
      throw new InvalidMenuException(
          "@Paginated method "
              + method.getName()
              + " must be either () -> List<MenuItem> or (int page, int pageSize) -> Page<MenuItem>");
    }
  }

  boolean isLazyProvider(Method method) {
    return hasTwoIntParameters(method) && returnsMenuItemPage(method);
  }

  void requireTick(Method method) {
    if (method.getParameterCount() != 0 || method.getReturnType() != void.class) {
      throw new InvalidMenuException(
          "@Tick method " + method.getName() + " must take no args and return void");
    }
  }

  private boolean isEagerProvider(Method method) {
    return method.getParameterCount() == 0 && returnsMenuItemList(method);
  }

  private boolean hasTwoIntParameters(Method method) {
    Class<?>[] parameters = method.getParameterTypes();
    return parameters.length == 2 && parameters[0] == int.class && parameters[1] == int.class;
  }

  private boolean returnsMenuItemList(Method method) {
    if (!List.class.isAssignableFrom(method.getReturnType())) {
      return false;
    }
    return elementType(method.getGenericReturnType()) == MenuItem.class;
  }

  private boolean returnsMenuItemPage(Method method) {
    if (!Page.class.isAssignableFrom(method.getReturnType())) {
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
