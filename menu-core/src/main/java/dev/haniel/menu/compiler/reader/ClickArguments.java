package dev.haniel.menu.compiler.reader;

import dev.haniel.menu.action.ButtonArguments;
import dev.haniel.menu.action.ClickArgumentResolver;
import dev.haniel.menu.compiler.InvalidMenuException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validates a {@code @Button} method signature and chooses how to supply its argument.
 *
 * <p>A button method must return {@code void} and take either no parameter or a single one of a
 * supported type. {@link dev.haniel.menu.click.ClickContext} is always supported; the platform
 * layer registers more types (such as the Bukkit player) by passing extra {@link
 * ClickArgumentResolver}s. Read once at boot.
 */
public final class ClickArguments {

  private static final Object[] NO_ARGUMENTS = new Object[0];

  private final List<ClickArgumentResolver> resolvers;
  private final Map<Method, ButtonArguments> cache = new ConcurrentHashMap<>();

  /**
   * Creates a registry over the given platform resolvers, plus the built-in context resolver.
   *
   * @param platformResolvers resolvers contributed by the platform layer; never null
   */
  public ClickArguments(List<ClickArgumentResolver> platformResolvers) {
    Objects.requireNonNull(platformResolvers, "platformResolvers");
    List<ClickArgumentResolver> all = new ArrayList<>();
    all.add(new ClickContextResolver());
    all.addAll(platformResolvers);
    this.resolvers = List.copyOf(all);
  }

  /**
   * Validates the given button method and returns how to supply its arguments per click.
   *
   * @param method the annotated button method; never null
   * @return an argument supplier: empty for a no-arg button, otherwise the resolved single value
   * @throws InvalidMenuException if it returns a value, takes more than one parameter, or takes a
   *     parameter type no resolver supports
   */
  public ButtonArguments bindingFor(Method method) {
    return cache.computeIfAbsent(method, this::computeBinding);
  }

  private ButtonArguments computeBinding(Method method) {
    requireVoid(method);
    requireAtMostOneParameter(method);
    if (method.getParameterCount() == 0) {
      return _ -> NO_ARGUMENTS;
    }
    ClickArgumentResolver resolver = resolverFor(method);
    return context -> new Object[] {resolver.resolve(context)};
  }

  private ClickArgumentResolver resolverFor(Method method) {
    Class<?> type = method.getParameterTypes()[0];
    return resolvers.stream()
        .filter(resolver -> resolver.supports(type))
        .findFirst()
        .orElseThrow(() -> new InvalidMenuException(unsupported(method, type)));
  }

  private void requireVoid(Method method) {
    if (method.getReturnType() != Void.TYPE) {
      throw new InvalidMenuException("@Button method " + method.getName() + " must return void");
    }
  }

  private void requireAtMostOneParameter(Method method) {
    if (method.getParameterCount() > 1) {
      throw new InvalidMenuException(
          "@Button method " + method.getName() + " must take no parameter or a single one");
    }
  }

  private static String unsupported(Method method, Class<?> type) {
    return "@Button method "
        + method.getName()
        + " parameter "
        + type.getSimpleName()
        + " is not injectable; use no parameter, ClickContext, or a platform type like Player/MenuClick";
  }
}
