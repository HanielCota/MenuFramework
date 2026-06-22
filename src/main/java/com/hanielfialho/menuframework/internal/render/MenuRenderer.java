package com.hanielfialho.menuframework.internal.render;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import java.util.Objects;
import org.bukkit.entity.Player;

/** Produz frames completos sem alterar diretamente o inventário Bukkit. */
public final class MenuRenderer {

  private MenuRenderer() {}

  public static <S> MenuFrame<S> render(Menu<S> menu, Player viewer, S state, int historyDepth) {
    Objects.requireNonNull(menu, "menu");

    MenuRenderContext<S> context = new MenuRenderContext<>(viewer, state, historyDepth);

    MenuLayout layout = Objects.requireNonNull(menu.layout(), "The menu returned a null layout");

    DefaultMenuCanvas<S> canvas = new DefaultMenuCanvas<>(layout);
    menu.render(context, canvas);
    return canvas.build();
  }

  public static <S> MenuFrame<S> render(Menu<S> menu, Player viewer, S state) {
    return render(menu, viewer, state, 0);
  }
}
