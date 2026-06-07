package dev.haniel.menu.compiler.binding;

import dev.haniel.menu.action.MenuActionException;
import dev.haniel.menu.item.MenuItem;
import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Objects;

/**
 * Supplies a paginated menu's content through the bound {@code @Paginated} method handle.
 *
 * <p>The handle is resolved once at boot; {@link #provide()} invokes it per render, so no
 * reflection happens on click. The provider yields data and actions, never cached presentation.
 *
 * <p><strong>Cache:</strong> this class does not cache the returned items. If the
 * {@code @Paginated} method performs expensive work (database queries, filtering, sorting), cache
 * the result in your menu class and invalidate it when the underlying data changes.
 */
public final class ContentProvider {

  private final MethodHandle handle;

  /**
   * Wraps a method handle of type {@code () -> List<MenuItem>} bound to the menu instance.
   *
   * @param handle the bound content handle; never null
   */
  public ContentProvider(MethodHandle handle) {
    this.handle = handle;
  }

  /**
   * Invokes the provider and returns its items.
   *
   * @return the current, unpaginated items
   * @throws MenuActionException if the provider throws
   */
  @SuppressWarnings("java:S1181") // MethodHandle invocation can throw any user-declared Throwable.
  public List<MenuItem> provide() {
    try {
      return requireItems(handle.invoke());
    } catch (Error error) {
      throw error;
    } catch (Throwable throwable) {
      throw new MenuActionException("Paginated content provider failed", throwable);
    }
  }

  @SuppressWarnings("unchecked") // the provider return type is validated at boot by the reader
  private static List<MenuItem> requireItems(Object result) {
    return Objects.requireNonNull(
        (List<MenuItem>) result, "Paginated content provider returned null");
  }
}
