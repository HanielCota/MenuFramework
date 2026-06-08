package dev.haniel.menu.compiler.reader;

import dev.haniel.menu.action.ButtonArguments;
import dev.haniel.menu.annotation.Button;
import dev.haniel.menu.annotation.Menu;
import dev.haniel.menu.annotation.Paginated;
import dev.haniel.menu.annotation.Reactive;
import dev.haniel.menu.annotation.Tick;
import dev.haniel.menu.annotation.Viewer;
import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.binding.ButtonGuards;
import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.binding.StateField;
import dev.haniel.menu.compiler.binding.UnboundAction;
import dev.haniel.menu.compiler.binding.UnboundProvider;
import dev.haniel.menu.compiler.binding.UnboundTick;
import dev.haniel.menu.compiler.binding.ViewerField;
import dev.haniel.menu.compiler.model.PagedStructure;
import dev.haniel.menu.domain.ButtonId;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.state.State;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Discovers the structure of a paginated menu class at boot.
 *
 * <p>The single home of reflection for the paginated path: it reads {@code @Paginated},
 * {@code @Button}, {@code @Reactive} and {@code @Viewer} into unbound handles and resolves the
 * no-arg constructor. Nothing here is bound to an instance — that happens per open.
 */
public final class PagedReader {

  private final MethodSignatureValidator validator = new MethodSignatureValidator();
  private final ClickArguments clickArguments;
  private final Caches caches = new Caches();

  /** Creates a reader supporting only the built-in {@code ClickContext} parameter. */
  public PagedReader() {
    this(new ClickArguments(List.of()));
  }

  /**
   * Creates a reader whose {@code @Button} parameters are resolved by the given registry.
   *
   * @param clickArguments the registry of injectable parameter types; never null
   */
  public PagedReader(ClickArguments clickArguments) {
    this.clickArguments = clickArguments;
  }

  /**
   * Tells whether the given class is a paginated menu.
   *
   * @param type the menu class
   * @return {@code true} if it declares a {@code @Paginated} method
   */
  public boolean handles(Class<?> type) {
    return caches.handles(type, this::hasPaginatedProvider);
  }

  /**
   * Reads the structure of the given paginated menu class.
   *
   * @param type the menu class
   * @return the discovered structure
   * @throws InvalidMenuException if the class is not a valid paginated menu
   */
  public PagedStructure read(Class<?> type) {
    PagedMetadata found = metadata(type);
    return found.structure(instantiator(type));
  }

  /**
   * Reads the structure of the given paginated menu class using the supplied instance factory.
   *
   * @param type the menu class
   * @param instantiator creates one fresh menu instance per open; never null
   * @return the discovered structure
   */
  public PagedStructure read(Class<?> type, Instantiator instantiator) {
    return metadata(type).structure(instantiator);
  }

  private boolean hasPaginatedProvider(Class<?> type) {
    return ReflectedMembers.methods(type).stream()
        .anyMatch(method -> method.isAnnotationPresent(Paginated.class));
  }

  private PagedMetadata metadata(Class<?> type) {
    return caches.metadata(
        type,
        key ->
            new PagedMetadata(
                readId(key), provider(key), buttons(key), states(key), ticks(key), viewers(key)));
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

  private Instantiator instantiator(Class<?> type) {
    return caches.instantiator(type, this::createInstantiator);
  }

  @SuppressWarnings("java:S3011") // Menu annotations intentionally support private constructors.
  private Instantiator createInstantiator(Class<?> type) {
    try {
      Constructor<?> constructor = type.getDeclaredConstructor();
      constructor.setAccessible(true);
      return new Instantiator(MethodHandles.lookup().unreflectConstructor(constructor));
    } catch (NoSuchMethodException | IllegalAccessException exception) {
      throw new InvalidMenuException(type.getName() + " needs a no-arg constructor", exception);
    }
  }

  private UnboundProvider provider(Class<?> type) {
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
    Method method = providers.getFirst();
    validateProvider(method);
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

  private List<UnboundTick> ticks(Class<?> type) {
    return ReflectedMembers.methods(type).stream()
        .filter(method -> method.isAnnotationPresent(Tick.class))
        .map(this::tick)
        .toList();
  }

  private UnboundTick tick(Method method) {
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
    } catch (IllegalAccessException exception) {
      throw new InvalidMenuException("Cannot access @Reactive field " + field.getName(), exception);
    }
  }

  @SuppressWarnings("java:S3011") // Viewer fields may be private implementation details.
  private ViewerField viewerField(Field field) {
    validateViewerField(field);
    try {
      field.setAccessible(true);
      return new ViewerField(field.getName(), MethodHandles.lookup().unreflectSetter(field));
    } catch (IllegalAccessException exception) {
      throw new InvalidMenuException("Cannot access @Viewer field " + field.getName(), exception);
    }
  }

  private void validateProvider(Method method) {
    validator.requirePaginatedProvider(method);
  }

  private void validateStateField(Field field) {
    if (!State.class.isAssignableFrom(field.getType())) {
      throw new InvalidMenuException("@Reactive field " + field.getName() + " must be State<?>");
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

  @SuppressWarnings("java:S3011") // Button/provider methods may be private annotated handlers.
  private MethodHandle unreflect(Method method) {
    try {
      method.setAccessible(true);
      return MethodHandles.lookup().unreflect(method);
    } catch (IllegalAccessException exception) {
      throw new InvalidMenuException("Cannot access method " + method.getName(), exception);
    }
  }

  private record PagedMetadata(
      MenuId id,
      UnboundProvider provider,
      Map<ButtonId, UnboundAction> buttons,
      List<StateField> states,
      List<UnboundTick> ticks,
      List<ViewerField> viewers) {

    PagedStructure structure(Instantiator instantiator) {
      return new PagedStructure(id, instantiator, provider, buttons, states, ticks, viewers);
    }
  }

  /**
   * The boot-reflection memo of this reader: the three per-class caches grouped so the reader keeps
   * a single field for its lazily-computed structure rather than three loose maps.
   */
  private static final class Caches {

    private final ConcurrentMap<Class<?>, Boolean> handles = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, PagedMetadata> metadata = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Instantiator> instantiators = new ConcurrentHashMap<>();

    boolean handles(Class<?> type, Function<Class<?>, Boolean> compute) {
      return handles.computeIfAbsent(type, compute);
    }

    PagedMetadata metadata(Class<?> type, Function<Class<?>, PagedMetadata> compute) {
      return metadata.computeIfAbsent(type, compute);
    }

    Instantiator instantiator(Class<?> type, Function<Class<?>, Instantiator> compute) {
      return instantiators.computeIfAbsent(type, compute);
    }
  }
}
