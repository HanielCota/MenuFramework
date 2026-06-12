package dev.haniel.menu.paper;

import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.paper.api.MenuOpener;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * A {@link MenuOpener} whose target is set once, after the registry exists.
 *
 * <p>The click resolver that hands {@link dev.haniel.menu.paper.api.MenuClick} its opener is built
 * while the registry is still being constructed, so the registry cannot be passed directly. This
 * forwarder bridges that one boot-time gap: the factory wires it into the resolver, then points it
 * at the registry once built. It is never invoked before {@link #delegateTo(MenuOpener)} (clicks
 * happen long after boot), so an early call is a framework bug and fails loudly.
 */
final class DeferredMenuOpener implements MenuOpener {

  private @Nullable MenuOpener target;

  void delegateTo(MenuOpener opener) {
    this.target = Objects.requireNonNull(opener, "opener");
  }

  @Override
  public void open(Player viewer, MenuId id) {
    require().open(viewer, id);
  }

  @Override
  public void open(Player viewer, Class<?> menuType) {
    require().open(viewer, menuType);
  }

  private MenuOpener require() {
    if (target == null) {
      throw new IllegalStateException("Menu opener used before the framework finished booting");
    }
    return target;
  }
}
