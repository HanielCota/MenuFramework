package dev.haniel.menu.compiler.reader;

import dev.haniel.menu.action.ButtonArguments;
import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.binding.ButtonActions;
import dev.haniel.menu.compiler.model.ButtonBehavior;
import dev.haniel.menu.compiler.model.MenuBlueprint;
import dev.haniel.menu.domain.ButtonId;
import dev.haniel.menu.domain.MenuId;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads a static menu's behaviour from an annotated class into a {@link MenuBlueprint}.
 *
 * <p>The single home of reflection for static menus: {@code @Menu}/{@code @Button} are read once
 * and each button method is bound to a {@link MethodHandle}. The resulting blueprint runs without
 * reflection. Intended to be called only at boot or reload. Paginated menus are read by {@link
 * PagedReader}.
 */
public final class StaticReader {

  private final ClickArguments clickArguments;

  /** Creates a reader supporting only the built-in {@code ClickContext} parameter. */
  public StaticReader() {
    this(new ClickArguments(List.of()));
  }

  /**
   * Creates a reader whose {@code @Button} parameters are resolved by the given registry.
   *
   * @param clickArguments the registry of injectable parameter types; never null
   */
  public StaticReader(ClickArguments clickArguments) {
    this.clickArguments = clickArguments;
  }

  /**
   * Reads the blueprint for the given annotated instance.
   *
   * @param instance an object whose class is annotated with {@code @Menu}; never null
   * @return the menu id and its button behaviours
   * @throws InvalidMenuException if the class is not annotated with {@code @Menu}
   */
  public MenuBlueprint read(Object instance) {
    Class<?> type = instance.getClass();
    return new MenuBlueprint(readId(type), behaviors(instance, type));
  }

  private MenuId readId(Class<?> type) {
    Menu menu = type.getAnnotation(Menu.class);
    if (menu == null) {
      throw new InvalidMenuException(type.getName() + " is not annotated with @Menu");
    }
    return new MenuId(menu.id());
  }

  private List<ButtonBehavior> behaviors(Object instance, Class<?> type) {
    Set<ButtonId> ids = new HashSet<>();
    return Arrays.stream(type.getDeclaredMethods())
        .filter(method -> method.isAnnotationPresent(Button.class))
        .map(method -> behavior(instance, method, ids))
        .toList();
  }

  private ButtonBehavior behavior(Object instance, Method method, Set<ButtonId> ids) {
    ButtonArguments arguments = clickArguments.bindingFor(method);
    ButtonId id = new ButtonId(method.getAnnotation(Button.class).id());
    if (!ids.add(id)) {
      throw new InvalidMenuException("Duplicate @Button id '" + id.value() + "'");
    }
    return new ButtonBehavior(id, ButtonActions.bind(bind(instance, method), arguments));
  }

  @SuppressWarnings("java:S3011") // Button methods may be private annotated handlers.
  private MethodHandle bind(Object instance, Method method) {
    try {
      method.setAccessible(true);
      return MethodHandles.lookup().unreflect(method).bindTo(instance);
    } catch (IllegalAccessException exception) {
      throw new InvalidMenuException(
          "Cannot access annotated method " + method.getName(), exception);
    }
  }
}
