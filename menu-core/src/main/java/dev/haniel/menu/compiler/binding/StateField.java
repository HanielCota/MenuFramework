package dev.haniel.menu.compiler.binding;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.state.State;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

/**
 * A boot-resolved getter for one {@code @Reactive} field, read per open.
 *
 * <p>The structure (which fields are state) is discovered at boot; the actual {@link State} value
 * is read from the fresh per-player instance when the menu opens.
 */
public final class StateField {

  private final MethodHandle getter;
  private final String name;

  /**
   * Wraps an unbound getter handle of type {@code (Instance) -> State}.
   *
   * @param name the source field name, used in validation messages; never null
   * @param getter the field getter handle; never null
   */
  public StateField(String name, MethodHandle getter) {
    this.name = Objects.requireNonNull(name, "name");
    this.getter = getter;
  }

  /**
   * Reads the state object from the given instance.
   *
   * @param instance the per-player menu instance; never null
   * @return the state held in the field
   * @throws InvalidMenuException if the field cannot be read
   */
  @SuppressWarnings("java:S1181") // MethodHandle invocation can throw any user-declared Throwable.
  public State<?> read(Object instance) {
    try {
      State<?> state = (State<?>) getter.invoke(instance);
      if (state == null) {
        throw new InvalidMenuException("@Reactive field " + name + " returned null");
      }
      return state;
    } catch (InvalidMenuException | Error exception) {
      throw exception;
    } catch (Throwable throwable) {
      throw new InvalidMenuException("Cannot read @Reactive field " + name, throwable);
    }
  }
}
