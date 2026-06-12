package dev.haniel.menu.paper.placeholder;

import dev.haniel.menu.domain.PlayerId;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.placeholder.PlaceholderResolver;
import dev.haniel.menu.template.IconFactory;
import java.util.List;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

/**
 * An {@link IconFactory} that resolves placeholders in an icon's text for one viewer before
 * building the item.
 *
 * <p>Bound to a single open view, so its per-view page cache stays correct: the same raw icon
 * resolves to that viewer's text. Icons without a placeholder are passed through untouched, so
 * non-placeholder content allocates nothing extra.
 */
public final class ResolvedIconFactory implements IconFactory<ItemStack> {

  private final IconFactory<ItemStack> delegate;
  private final PlaceholderResolver placeholders;
  private final PlayerId viewer;

  /**
   * Wraps the delegate with per-viewer placeholder resolution.
   *
   * @param delegate the factory that builds the final item; never null
   * @param placeholders the resolver applied to name and lore; never null
   * @param viewer the player the icons are rendered for; never null
   */
  public ResolvedIconFactory(
      IconFactory<ItemStack> delegate, PlaceholderResolver placeholders, PlayerId viewer) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
    this.placeholders = Objects.requireNonNull(placeholders, "placeholders");
    this.viewer = Objects.requireNonNull(viewer, "viewer");
  }

  @Override
  public ItemStack create(Icon icon) {
    return delegate.create(icon.hasPlaceholder() ? resolved(icon) : icon);
  }

  private Icon resolved(Icon icon) {
    String name = placeholders.resolve(viewer, icon.name());
    List<String> lore =
        icon.lore().stream().map(line -> placeholders.resolve(viewer, line)).toList();
    return new Icon(icon.material(), name, lore, icon.traits());
  }
}
