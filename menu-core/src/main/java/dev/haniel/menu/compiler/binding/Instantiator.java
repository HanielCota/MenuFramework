package dev.haniel.menu.compiler.binding;

import dev.haniel.menu.compiler.InvalidMenuException;
import java.lang.invoke.MethodHandle;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Creates fresh menu instances from a boot-resolved no-arg constructor handle.
 *
 * <p>Paginated menus get one instance per open so each player owns independent {@code State}. The
 * constructor is reflected once at boot; {@link #create()} only invokes the handle.
 */
public final class Instantiator {

  private final Supplier<Object> instances;

  /**
   * Wraps a no-arg constructor handle of type {@code () -> Instance}.
   *
   * @param constructor the bound-free constructor handle; never null
   */
  public Instantiator(MethodHandle constructor) {
    this(() -> invoke(constructor));
  }

  /**
   * Wraps a factory that creates one fresh menu instance.
   *
   * @param instances the instance factory; never null
   */
  public Instantiator(Supplier<Object> instances) {
    this.instances = Objects.requireNonNull(instances, "instances");
  }

  /**
   * Builds a new menu instance.
   *
   * @return a fresh instance
   * @throws InvalidMenuException if construction fails
   */
  @SuppressWarnings("java:S1181") // Custom suppliers may be backed by MethodHandle constructors.
  public Object create() {
    try {
      return Objects.requireNonNull(instances.get(), "Menu instantiator returned null");
    } catch (RuntimeException | Error exception) {
      throw exception;
    } catch (Throwable throwable) {
      throw new InvalidMenuException("Cannot instantiate menu", throwable);
    }
  }

  @SuppressWarnings("java:S1181") // MethodHandle invocation can throw any user-declared Throwable.
  private static Object invoke(MethodHandle constructor) {
    try {
      return constructor.invoke();
    } catch (Error error) {
      throw error;
    } catch (Throwable throwable) {
      throw new InvalidMenuException("Cannot instantiate menu", throwable);
    }
  }
}
