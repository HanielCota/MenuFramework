package dev.haniel.menu.compiler.reader;

import dev.haniel.menu.action.ButtonArguments;
import dev.haniel.menu.action.MenuAction;
import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.OnClose;
import dev.haniel.menu.annotation.OnOpen;
import dev.haniel.menu.annotation.Reactive;
import dev.haniel.menu.annotation.Tick;
import dev.haniel.menu.annotation.Viewer;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.binding.ButtonActions;
import dev.haniel.menu.compiler.binding.ButtonGuards;
import dev.haniel.menu.compiler.model.ButtonBehavior;
import dev.haniel.menu.compiler.model.MenuBlueprint;
import dev.haniel.menu.domain.ButtonId;
import dev.haniel.menu.domain.MenuId;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
  private final Map<Class<?>, List<Method>> buttonMethods = new ConcurrentHashMap<>();

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
    this.clickArguments = Objects.requireNonNull(clickArguments, "clickArguments");
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
    rejectReactiveOnlyAnnotations(type);
    return new MenuBlueprint(readId(type), behaviors(instance, type));
  }

  private MenuId readId(Class<?> type) {
    Menu menu = findMenu(type);
    if (menu == null) {
      throw new InvalidMenuException(type.getName() + " is not annotated with @Menu");
    }
    try {
      return new MenuId(menu.id());
    } catch (IllegalArgumentException exception) {
      throw new InvalidMenuException(
          "@Menu id on " + type.getName() + " is invalid: " + exception.getMessage(), exception);
    }
  }

  private Menu findMenu(Class<?> type) {
    Class<?> current = type;
    while (current != null && current != Object.class) {
      Menu menu = current.getAnnotation(Menu.class);
      if (menu != null) {
        return menu;
      }
      current = current.getSuperclass();
    }
    return null;
  }

  private List<ButtonBehavior> behaviors(Object instance, Class<?> type) {
    Set<ButtonId> ids = new HashSet<>();
    return buttonMethods(type).stream().map(method -> behavior(instance, method, ids)).toList();
  }

  private List<Method> buttonMethods(Class<?> type) {
    return buttonMethods.computeIfAbsent(type, this::discoverButtons);
  }

  private List<Method> discoverButtons(Class<?> type) {
    return ReflectedMembers.methods(type).stream()
        .filter(method -> method.isAnnotationPresent(Button.class))
        .toList();
  }

  private ButtonBehavior behavior(Object instance, Method method, Set<ButtonId> ids) {
    ButtonArguments arguments = clickArguments.bindingFor(method);
    Button button = method.getAnnotation(Button.class);
    ButtonId id = buttonId(method, button);
    if (!ids.add(id)) {
      throw new InvalidMenuException("Duplicate @Button id '" + id.value() + "'");
    }
    MenuAction action = ButtonActions.bind(bind(instance, method), arguments);
    ButtonGuards guards = new ButtonGuards(button.permission(), button.cooldownMillis());
    return new ButtonBehavior(id, guards.apply(action));
  }

  private ButtonId buttonId(Method method, Button button) {
    try {
      return new ButtonId(button.id());
    } catch (IllegalArgumentException exception) {
      throw new InvalidMenuException(
          "@Button id on "
              + method.getDeclaringClass().getName()
              + "#"
              + method.getName()
              + " is invalid: "
              + exception.getMessage(),
          exception);
    }
  }

  private void rejectReactiveOnlyAnnotations(Class<?> type) {
    List<Method> methods = ReflectedMembers.methods(type);
    rejectMethod(type, methods, Tick.class, "@Tick");
    rejectMethod(type, methods, OnOpen.class, "@OnOpen");
    rejectMethod(type, methods, OnClose.class, "@OnClose");
    rejectField(type, Reactive.class, "@Reactive");
    rejectField(type, Viewer.class, "@Viewer");
  }

  private void rejectField(Class<?> type, Class<? extends Annotation> annotation, String label) {
    ReflectedMembers.fields(type).stream()
        .filter(field -> field.isAnnotationPresent(annotation))
        .findFirst()
        .ifPresent(field -> failStaticOnly(type, label, field.getName()));
  }

  private void rejectMethod(
      Class<?> type, List<Method> methods, Class<? extends Annotation> annotation, String label) {
    methods.stream()
        .filter(method -> method.isAnnotationPresent(annotation))
        .findFirst()
        .ifPresent(method -> failStaticOnly(type, label, method.getName()));
  }

  private void failStaticOnly(Class<?> type, String annotation, String member) {
    throw new InvalidMenuException(
        annotation
            + " member "
            + member
            + " on static menu "
            + type.getName()
            + " requires a @Paginated menu");
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
