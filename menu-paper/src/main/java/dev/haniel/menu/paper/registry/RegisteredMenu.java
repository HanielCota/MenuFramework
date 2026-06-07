package dev.haniel.menu.paper.registry;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.view.PaperMenu;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * A registered menu: the annotated source kept for reloads and the current openable menu.
 *
 * <p>The openable is held in an {@link AtomicReference} so a reload can swap in a freshly compiled
 * one atomically. Already-open views keep their old menu until reopened.
 */
public final class RegisteredMenu {

  private final MenuId id;
  private final Object source;
  private final Class<?> sourceType;
  private final Supplier<Object> sourceFactory;
  private final AtomicReference<PaperMenu> current;

  /**
   * Creates a registered menu.
   *
   * @param id the menu id; never null
   * @param source the annotated instance, kept to recompile on reload; never null
   * @param initial the first compiled menu; never null
   */
  public RegisteredMenu(MenuId id, Object source, PaperMenu initial) {
    this.id = id;
    this.source = source;
    this.sourceType = source.getClass();
    this.sourceFactory = () -> source;
    this.current = new AtomicReference<>(initial);
  }

  /**
   * Creates a registered menu backed by a source factory.
   *
   * @param id the menu id; never null
   * @param sourceType the annotated menu class; never null
   * @param sourceFactory creates instances using the configured strategy; never null
   * @param initial the first compiled menu; never null
   */
  public RegisteredMenu(
      MenuId id, Class<?> sourceType, Supplier<Object> sourceFactory, PaperMenu initial) {
    this.id = id;
    this.source = null;
    this.sourceType = sourceType;
    this.sourceFactory = sourceFactory;
    this.current = new AtomicReference<>(initial);
  }

  /**
   * Returns the menu id.
   *
   * @return the registered id
   */
  public MenuId id() {
    return id;
  }

  /**
   * Returns the annotated source instance.
   *
   * @return the source used to recompile this menu
   */
  public Object source() {
    return source;
  }

  /**
   * Returns the annotated source class.
   *
   * @return the source type
   */
  public Class<?> sourceType() {
    return sourceType;
  }

  /**
   * Creates a source instance for recompilation.
   *
   * @return a menu instance
   */
  public Object createSource() {
    return sourceFactory.get();
  }

  /**
   * Checks whether this menu was registered from the given class.
   *
   * @param sourceType the annotated menu class; never null
   * @return {@code true} when the stored source is an instance of that exact class
   */
  public boolean hasSourceType(Class<?> sourceType) {
    return this.sourceType.equals(sourceType);
  }

  /**
   * Returns the current openable menu.
   *
   * @return the latest compiled menu
   */
  public PaperMenu current() {
    return current.get();
  }

  /**
   * Atomically replaces the current openable menu.
   *
   * @param next the newly compiled menu; never null
   */
  public void swap(PaperMenu next) {
    current.set(next);
  }
}
