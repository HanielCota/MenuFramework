package dev.haniel.menu.compiler.reader;

import dev.haniel.menu.action.ButtonArguments;
import dev.haniel.menu.annotation.Arg;
import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.Paginated;
import dev.haniel.menu.annotation.Reactive;
import dev.haniel.menu.annotation.Tick;
import dev.haniel.menu.annotation.Viewer;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.binding.ArgField;
import dev.haniel.menu.compiler.binding.ButtonGuards;
import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.binding.StateField;
import dev.haniel.menu.compiler.binding.UnboundAction;
import dev.haniel.menu.compiler.binding.UnboundContent;
import dev.haniel.menu.compiler.binding.UnboundPageProvider;
import dev.haniel.menu.compiler.binding.UnboundProvider;
import dev.haniel.menu.compiler.binding.UnboundTick;
import dev.haniel.menu.compiler.binding.ViewerField;
import dev.haniel.menu.domain.ButtonId;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.state.State;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * The reflection behind the paginated path: reads {@code @Paginated}, {@code @Button},
 * {@code @Reactive}, {@code @Tick} and {@code @Viewer} into unbound handles and resolves the no-arg
 * constructor.
 *
 * <p>Stateless across classes; {@link PagedReader} owns the per-class caching so this type performs
 * the raw reading on demand. Nothing here is bound to an instance — that happens per open.
 */
final class PagedStructureReader {

  private final MethodSignatureValidator validator = new MethodSignatureValidator();
  private final ClickArguments clickArguments;

  PagedStructureReader(ClickArguments clickArguments) {
    this.clickArguments = Objects.requireNonNull(clickArguments, "clickArguments");
  }

  boolean hasPaginatedProvider(Class<?> type) {
    return ReflectedMembers.methods(type).stream()
        .anyMatch(method -> method.isAnnotationPresent(Paginated.class));
  }

  PagedMetadata readMetadata(Class<?> type) {
    return new PagedMetadata(
        readId(type),
        provider(type),
        buttons(type),
        states(type),
        ticks(type),
        viewers(type),
        args(type));
  }

  @SuppressWarnings("java:S3011") // Menu annotations intentionally support private constructors.
  Instantiator createInstantiator(Class<?> type) {
    if (Modifier.isAbstract(type.getModifiers())) {
      throw new InvalidMenuException(
          type.getName() + " must be a concrete class to open as a menu");
    }
    try {
      Constructor<?> constructor = type.getDeclaredConstructor();
      constructor.setAccessible(true);
      return new Instantiator(MethodHandles.lookup().unreflectConstructor(constructor));
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InaccessibleObjectException exception) {
      throw new InvalidMenuException(type.getName() + " needs a no-arg constructor", exception);
    }
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

  private @Nullable Menu findMenu(Class<?> type) {
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

  private UnboundContent provider(Class<?> type) {
    List<Method> providers =
        ReflectedMembers.methods(type).stream()
            .filter(candidate -> candidate.isAnnotationPresent(Paginated.class))
            .toList();
    if (providers.isEmpty()) {
      throw new InvalidMenuException(type.getName() + " has no @Paginated method");
    }
    if (providers.size() > 1) {
      throw new InvalidMenuException(type.getName() + " has more than one @Paginated method");
    }
    return toProvider(providers.getFirst());
  }

  private UnboundContent toProvider(Method method) {
    MethodSignatureValidator.requireInstanceMethod(method, "@Paginated");
    validateProvider(method);
    if (validator.isLazyProvider(method)) {
      return new UnboundPageProvider(unreflect(method));
    }
    return new UnboundProvider(unreflect(method));
  }

  private Map<ButtonId, UnboundAction> buttons(Class<?> type) {
    Map<ButtonId, UnboundAction> buttons = new HashMap<>();
    ReflectedMembers.methods(type).stream()
        .filter(method -> method.isAnnotationPresent(Button.class))
        .forEach(method -> addButton(buttons, method));
    return Map.copyOf(buttons);
  }

  private void addButton(Map<ButtonId, UnboundAction> buttons, Method method) {
    MethodSignatureValidator.requireInstanceMethod(method, "@Button");
    ButtonArguments arguments = clickArguments.bindingFor(method);
    Button button = method.getAnnotation(Button.class);
    ButtonId id = buttonId(method, button);
    if (buttons.containsKey(id)) {
      throw new InvalidMenuException("Duplicate @Button id '" + id.value() + "'");
    }
    ButtonGuards guards = new ButtonGuards(button.permission(), button.cooldownMillis());
    buttons.put(id, new UnboundAction(unreflect(method), arguments, guards));
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

  private List<StateField> states(Class<?> type) {
    return ReflectedMembers.fields(type).stream()
        .filter(field -> field.isAnnotationPresent(Reactive.class))
        .map(this::stateField)
        .toList();
  }

  private List<ViewerField> viewers(Class<?> type) {
    return ReflectedMembers.fields(type).stream()
        .filter(field -> field.isAnnotationPresent(Viewer.class))
        .map(this::viewerField)
        .toList();
  }

  private List<ArgField> args(Class<?> type) {
    return ReflectedMembers.fields(type).stream()
        .filter(field -> field.isAnnotationPresent(Arg.class))
        .map(this::argField)
        .toList();
  }

  private List<UnboundTick> ticks(Class<?> type) {
    return ReflectedMembers.methods(type).stream()
        .filter(method -> method.isAnnotationPresent(Tick.class))
        .map(this::tick)
        .toList();
  }

  private UnboundTick tick(Method method) {
    MethodSignatureValidator.requireInstanceMethod(method, "@Tick");
    validator.requireTick(method);
    int period = method.getAnnotation(Tick.class).period();
    if (period < 1) {
      throw new InvalidMenuException("@Tick period on " + method.getName() + " must be >= 1");
    }
    return new UnboundTick(unreflect(method), period);
  }

  @SuppressWarnings("java:S3011") // Reactive state fields may be private implementation details.
  private StateField stateField(Field field) {
    validateStateField(field);
    try {
      field.setAccessible(true);
      return new StateField(field.getName(), MethodHandles.lookup().unreflectGetter(field));
    } catch (IllegalAccessException | InaccessibleObjectException exception) {
      throw new InvalidMenuException("Cannot access @Reactive field " + field.getName(), exception);
    }
  }

  @SuppressWarnings("java:S3011") // Viewer fields may be private implementation details.
  private ViewerField viewerField(Field field) {
    validateViewerField(field);
    try {
      field.setAccessible(true);
      return new ViewerField(field.getName(), MethodHandles.lookup().unreflectSetter(field));
    } catch (IllegalAccessException | InaccessibleObjectException exception) {
      throw new InvalidMenuException("Cannot access @Viewer field " + field.getName(), exception);
    }
  }

  @SuppressWarnings("java:S3011") // Arg fields may be private implementation details.
  private ArgField argField(Field field) {
    validateArgField(field);
    try {
      field.setAccessible(true);
      MethodHandle setter = MethodHandles.lookup().unreflectSetter(field);
      return new ArgField(field.getName(), field.getType(), setter);
    } catch (IllegalAccessException | InaccessibleObjectException exception) {
      throw new InvalidMenuException("Cannot access @Arg field " + field.getName(), exception);
    }
  }

  private void validateProvider(Method method) {
    validator.requirePaginatedProvider(method);
  }

  private void validateStateField(Field field) {
    if (!State.class.isAssignableFrom(field.getType())) {
      throw new InvalidMenuException("@Reactive field " + field.getName() + " must be State<?>");
    }
    if (Modifier.isStatic(field.getModifiers())) {
      throw new InvalidMenuException(
          "@Reactive field "
              + field.getName()
              + " must be non-static; a static State would be"
              + " shared across every viewer's open view");
    }
  }

  private void validateViewerField(Field field) {
    if (field.getType() != PlayerId.class) {
      throw new InvalidMenuException("@Viewer field " + field.getName() + " must be PlayerId");
    }
    if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
      throw new InvalidMenuException(
          "@Viewer field " + field.getName() + " must be non-final and non-static");
    }
  }

  private void validateArgField(Field field) {
    if (field.getType().isPrimitive()) {
      throw new InvalidMenuException(
          "@Arg field " + field.getName() + " must be a reference type, not a primitive");
    }
    if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
      throw new InvalidMenuException(
          "@Arg field " + field.getName() + " must be non-final and non-static");
    }
  }

  @SuppressWarnings("java:S3011") // Button/provider methods may be private annotated handlers.
  private MethodHandle unreflect(Method method) {
    try {
      method.setAccessible(true);
      return MethodHandles.lookup().unreflect(method);
    } catch (IllegalAccessException | InaccessibleObjectException exception) {
      throw new InvalidMenuException("Cannot access method " + method.getName(), exception);
    }
  }
}
