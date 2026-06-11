package dev.haniel.menu.compiler.binding;

import dev.haniel.menu.compiler.InvalidMenuException;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

/**
 * A boot-resolved setter for one {@code @Arg} field, written once per open.
 *
 * <p>The structure (which fields receive the open argument and their declared types) is discovered
 * at boot; the actual value is written into the fresh per-player instance when the menu opens,
 * before its first render. A field only receives the argument when {@link #accepts(Object)} holds,
 * so a menu may declare several {@code @Arg} fields of different types and each takes the matching
 * open argument.
 */
public final class ArgField {

  private final String name;
  private final Class<?> type;
  private final MethodHandle setter;

  /**
   * Wraps an unbound setter handle of type {@code (Instance, type) -> void}.
   *
   * @param name the source field name, used in error messages; never null
   * @param type the declared field type, used to match the open argument; never null
   * @param setter the field setter handle; never null
   */
  public ArgField(String name, Class<?> type, MethodHandle setter) {
    this.name = Objects.requireNonNull(name, "name");
    this.type = Objects.requireNonNull(type, "type");
    this.setter = Objects.requireNonNull(setter, "setter");
  }

  /**
   * Tells whether the given open argument can be written into this field.
   *
   * @param argument the open argument; may be null
   * @return {@code true} if the argument is a non-null instance of the field's declared type
   */
  public boolean accepts(Object argument) {
    return type.isInstance(argument);
  }

  /**
   * Writes the open argument into the given instance.
   *
   * @param instance the per-player menu instance; never null
   * @param argument the open argument; must satisfy {@link #accepts(Object)}
   * @throws InvalidMenuException if the field cannot be written
   */
  @SuppressWarnings("java:S1181") // MethodHandle invocation can throw any user-declared Throwable.
  public void inject(Object instance, Object argument) {
    try {
      setter.invoke(instance, argument);
    } catch (Error error) {
      throw error;
    } catch (Throwable throwable) {
      throw new InvalidMenuException("Cannot write @Arg field " + name, throwable);
    }
  }
}
