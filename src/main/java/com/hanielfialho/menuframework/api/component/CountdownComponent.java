package com.hanielfialho.menuframework.api.component;

import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuOpenContext;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import com.hanielfialho.menuframework.api.task.MenuTaskKey;
import com.hanielfialho.menuframework.api.task.MenuTaskSchedule;
import com.hanielfialho.menuframework.api.task.MenuTickResult;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Menu that counts down from a number of seconds and optionally notifies when it reaches zero.
 *
 * @param <S> external state type, normally {@link Integer} or a record wrapping it
 */
public final class CountdownComponent<S> implements Menu<S> {

  private final String title;
  private final int rows;
  private final String slot;
  private final Function<? super S, Integer> secondsReader;
  private final Function<? super Integer, ? extends S> stateFactory;
  private final Consumer<? super S> onFinish;
  private final MenuTaskKey taskKey;

  private CountdownComponent(Builder<S> builder) {
    this.title = builder.title;
    this.rows = builder.rows;
    this.slot = builder.slot;
    this.secondsReader = builder.secondsReader;
    this.stateFactory = builder.stateFactory;
    this.onFinish = builder.onFinish;
    this.taskKey = builder.taskKey;
  }

  /**
   * Starts a builder for a countdown menu.
   *
   * @param title menu title
   * @param <S> state type
   * @return builder
   */
  public static <S> Builder<S> builder(String title) {
    return new Builder<>(title);
  }

  @Override
  public MenuLayout layout() {
    return MenuLayout.chest(this.rows);
  }

  @Override
  public Component title(MenuRenderContext<S> context) {
    return Component.text(this.title, NamedTextColor.GOLD);
  }

  @Override
  public void onOpen(MenuOpenContext<S> context) {
    context.repeat(
        this.taskKey,
        MenuTaskSchedule.everyTicks(20L),
        tick -> {
          int next = Math.max(0, this.secondsReader.apply(tick.state()) - 1);
          if (next == 0) {
            S finished = this.stateFactory.apply(0);
            this.onFinish.accept(finished);
            return MenuTickResult.stopWithState(finished);
          }
          return MenuTickResult.update(this.stateFactory.apply(next));
        });
  }

  @Override
  public void render(MenuRenderContext<S> context, MenuCanvas<S> canvas) {
    int seconds = this.secondsReader.apply(context.state());
    boolean finished = seconds == 0;

    ItemStack icon = new ItemStack(finished ? Material.EMERALD_BLOCK : Material.CLOCK);
    icon.editMeta(
        meta ->
            meta.displayName(
                finished
                    ? Component.text("Finalizado", NamedTextColor.GREEN)
                    : Component.text("Faltam " + seconds + "s", NamedTextColor.YELLOW)));

    canvas.item(this.slot, icon);
  }

  /** Mutable builder for {@link CountdownComponent}. */
  public static final class Builder<S> {

    private final String title;
    private int rows = 3;
    private String slot = "timer";
    private Function<? super S, Integer> secondsReader;
    private Function<? super Integer, ? extends S> stateFactory;
    private Consumer<? super S> onFinish = state -> {};
    private MenuTaskKey taskKey = MenuTaskKey.of("countdown");

    private Builder(String title) {
      this.title = Objects.requireNonNull(title, "title");
    }

    public Builder<S> rows(int rows) {
      if (rows < 1 || rows > 6) {
        throw new IllegalArgumentException("rows must be between 1 and 6: " + rows);
      }
      this.rows = rows;
      return this;
    }

    public Builder<S> slot(String slot) {
      this.slot = Objects.requireNonNull(slot, "slot");
      return this;
    }

    public Builder<S> secondsReader(Function<? super S, Integer> reader) {
      this.secondsReader = Objects.requireNonNull(reader, "reader");
      return this;
    }

    public Builder<S> stateFactory(Function<? super Integer, ? extends S> factory) {
      this.stateFactory = Objects.requireNonNull(factory, "factory");
      return this;
    }

    public Builder<S> onFinish(Consumer<? super S> onFinish) {
      this.onFinish = Objects.requireNonNull(onFinish, "onFinish");
      return this;
    }

    public Builder<S> taskKey(MenuTaskKey taskKey) {
      this.taskKey = Objects.requireNonNull(taskKey, "taskKey");
      return this;
    }

    public CountdownComponent<S> build() {
      Objects.requireNonNull(this.secondsReader, "secondsReader must be configured");
      Objects.requireNonNull(this.stateFactory, "stateFactory must be configured");
      return new CountdownComponent<>(this);
    }
  }
}
