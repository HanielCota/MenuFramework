package dev.haniel.menu.compiler.binding;

import dev.haniel.menu.action.MenuActionException;
import dev.haniel.menu.domain.Page;
import dev.haniel.menu.item.MenuItem;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

/**
 * Loads a paginated menu's content one page at a time through the bound {@code @Paginated} method
 * handle.
 *
 * <p>The handle is resolved once at boot; {@link #load(int, int)} invokes it per page. The provider
 * call may block (a database query), so the framework runs it off the view's thread and applies the
 * rendered page back on that thread — never call {@code load} from the main thread yourself.
 */
public final class PageProvider implements BoundContent {

  private final MethodHandle handle;

  /**
   * Wraps a method handle of type {@code (int, int) -> Page<MenuItem>} bound to the menu instance.
   *
   * @param handle the bound page handle; never null
   */
  public PageProvider(MethodHandle handle) {
    this.handle = Objects.requireNonNull(handle, "handle");
  }

  /**
   * Loads the requested page.
   *
   * @param page the zero-based page index to load
   * @param pageSize the number of content slots per page
   * @return the page's items and whether a further page exists; never null
   * @throws MenuActionException if the provider throws
   */
  @SuppressWarnings("java:S1181") // MethodHandle invocation can throw any user-declared Throwable.
  public Page<MenuItem> load(int page, int pageSize) {
    try {
      return requirePage(handle.invoke(page, pageSize));
    } catch (Error error) {
      throw error;
    } catch (Throwable throwable) {
      throw new MenuActionException("Lazy page provider failed", throwable);
    }
  }

  @SuppressWarnings("unchecked") // the provider return type is validated at boot by the reader
  private static Page<MenuItem> requirePage(Object result) {
    return Objects.requireNonNull((Page<MenuItem>) result, "Lazy page provider returned null");
  }
}
