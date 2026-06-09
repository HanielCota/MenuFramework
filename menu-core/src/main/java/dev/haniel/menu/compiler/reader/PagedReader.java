package dev.haniel.menu.compiler.reader;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.model.PagedStructure;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Discovers the structure of a paginated menu class at boot, caching the result per class.
 *
 * <p>The single home of the paginated path: it memoizes what {@link PagedStructureReader} reads via
 * reflection so a repeated open does no reflection. Nothing here is bound to an instance — that
 * happens per open.
 */
public final class PagedReader {

  private final PagedStructureReader reader;
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
    this.reader = new PagedStructureReader(clickArguments);
  }

  /**
   * Tells whether the given class is a paginated menu.
   *
   * @param type the menu class
   * @return {@code true} if it declares a {@code @Paginated} method
   */
  public boolean handles(Class<?> type) {
    return caches.handles(type, reader::hasPaginatedProvider);
  }

  /**
   * Reads the structure of the given paginated menu class.
   *
   * @param type the menu class
   * @return the discovered structure
   * @throws InvalidMenuException if the class is not a valid paginated menu
   */
  public PagedStructure read(Class<?> type) {
    return metadata(type).structure(caches.instantiator(type, reader::createInstantiator));
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

  private PagedMetadata metadata(Class<?> type) {
    return caches.metadata(type, reader::readMetadata);
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
