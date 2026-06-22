package com.hanielfialho.menuframework.internal.render;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.theme.MenuTheme;
import java.util.Objects;
import org.bukkit.entity.Player;

/** Produces complete frames without mutating a Bukkit inventory. */
public final class MenuRenderer {

  private MenuRenderer() {}

  public static <S> MenuFrame<S> render(
      Menu<S> menu, Player viewer, S state, int historyDepth, MenuTheme theme) {
    Objects.requireNonNull(menu, "menu");

    MenuRenderContext<S> context = new MenuRenderContext<>(viewer, state, historyDepth, theme);

    MenuLayout layout = Objects.requireNonNull(menu.layout(), "The menu returned a null layout");

    DefaultMenuCanvas<S> canvas = new DefaultMenuCanvas<>(layout);
    menu.render(context, canvas);
    return canvas.build();
  }

  public static <S> MenuFrame<S> render(Menu<S> menu, Player viewer, S state, int historyDepth) {
    Objects.requireNonNull(menu, "menu");
    MenuTheme theme =
        Objects.requireNonNull(menu.theme(MenuTheme.defaults()), "The menu returned a null theme");
    return render(menu, viewer, state, historyDepth, theme);
  }

  public static <S> MenuFrame<S> render(Menu<S> menu, Player viewer, S state) {
    return render(menu, viewer, state, 0);
  }
}
