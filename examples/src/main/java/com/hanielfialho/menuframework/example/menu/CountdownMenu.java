package com.hanielfialho.menuframework.example.menu;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuOpenContext;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.component.MenuComponents;
import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import com.hanielfialho.menuframework.api.task.MenuTaskSchedule;
import com.hanielfialho.menuframework.api.task.MenuTickResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.jspecify.annotations.NonNull;

/** Exemplo pequeno de task periódica pertencente à sessão. */
public final class CountdownMenu implements Menu<CountdownMenu.State> {

  private static final MenuLayout LAYOUT =
      MenuLayout.chestBuilder(3).slot("timer", 13).slot("close", 22).build();
  private static final MenuTaskKey COUNTDOWN = MenuTaskKey.of("countdown");

  @Override
  public MenuLayout layout() {
    return LAYOUT;
  }

  @Override
  public Component title(@NonNull MenuRenderContext<State> context) {
    return Component.text("Contagem", NamedTextColor.GOLD);
  }

  @Override
  public void onOpen(MenuOpenContext<State> context) {
    context.repeat(
        COUNTDOWN,
        MenuTaskSchedule.everyTicks(20L),
        tick -> {
          int nextSeconds = Math.max(0, tick.state().seconds() - 1);

          if (nextSeconds == 0) {
            return MenuTickResult.stopWithState(new State(0));
          }

          return MenuTickResult.update(new State(nextSeconds));
        });
  }

  @Override
  public void render(MenuRenderContext<State> context, MenuCanvas<State> canvas) {
    State state = context.state();

    canvas.component(context, MenuComponents.background());

    canvas.item(
        "timer",
        ItemStacks.named(
            state.seconds() == 0 ? Material.EMERALD_BLOCK : Material.CLOCK,
            state.seconds() == 0
                ? Component.text("Finalizado", NamedTextColor.GREEN)
                : Component.text("Faltam " + state.seconds() + "s", NamedTextColor.YELLOW)));

    canvas.component(context, MenuComponents.closeButton("close"));
  }

  public record State(int seconds) {

    public State {
      if (seconds < 0) {
        throw new IllegalArgumentException("seconds must be >= 0: " + seconds);
      }
    }
  }
}
