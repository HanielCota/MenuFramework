package dev.haniel.menu.compiler.binding;

import dev.haniel.menu.compiler.InvalidMenuException;
import dev.haniel.menu.domain.PlayerId;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

/**
 * A boot-resolved setter for one {@code @Viewer} field, written once per open.
 *
 * <p>The structure (which fields receive the viewer) is discovered at boot; the actual {@link
 * PlayerId} is written into the fresh per-player instance when the menu opens, before its first
 * render.
 */
public final class ViewerField {

  private final MethodHandle setter;
  private final String name;

  /**
   * Wraps an unbound setter handle of type {@code (Instance, PlayerId) -> void}.
   *
   * @param name the source field name, used in error messages; never null
   * @param setter the field setter handle; never null
   */
  public ViewerField(String name, MethodHandle setter) {
    this.name = Objects.requireNonNull(name, "name");
    this.setter = Objects.requireNonNull(setter, "setter");
  }

  /**
   * Writes the viewer into the given instance.
   *
   * @param instance the per-player menu instance; never null
   * @param viewer the viewing player's identifier; never null
   * @throws InvalidMenuException if the field cannot be written
   */
  @SuppressWarnings("java:S1181") // MethodHandle invocation can throw any user-declared Throwable.
  public void inject(Object instance, PlayerId viewer) {
    try {
      setter.invoke(instance, viewer);
    } catch (Error error) {
      throw error;
    } catch (Throwable throwable) {
      throw new InvalidMenuException("Cannot write @Viewer field " + name, throwable);
    }
  }
}
