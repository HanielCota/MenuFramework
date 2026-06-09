package dev.haniel.menu.template;

import dev.haniel.menu.compiler.binding.ContentProvider;
import dev.haniel.menu.item.MenuItem;
import java.util.List;
import java.util.Objects;

/**
 * The dynamic half of a paginated template: where content comes from and how it is rendered.
 *
 * <p>Pairs the boot-bound {@link ContentProvider} with the {@link IconFactory} used to turn each
 * item's appearance into a platform visual. The provider is called per render for fresh actions;
 * the visuals it produces are cached by the platform layer.
 *
 * @param <V> the platform visual type
 */
public final class PagedContent<V> {

  private final ContentProvider provider;
  private final IconFactory<V> icons;

  /**
   * Pairs the content source with its visual factory.
   *
   * @param provider the boot-bound content provider; never null
   * @param icons the factory rendering each item's icon; never null
   */
  public PagedContent(ContentProvider provider, IconFactory<V> icons) {
    this.provider = Objects.requireNonNull(provider, "provider");
    this.icons = Objects.requireNonNull(icons, "icons");
  }

  /**
   * Returns the current, unpaginated items.
   *
   * @return the items from the provider
   */
  public List<MenuItem> items() {
    return provider.provide();
  }

  /**
   * Renders a single item's appearance into a platform visual.
   *
   * @param item the item to render; never null
   * @return the built visual
   */
  public V render(MenuItem item) {
    return icons.create(item.icon());
  }
}
