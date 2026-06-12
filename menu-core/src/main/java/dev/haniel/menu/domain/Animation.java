package dev.haniel.menu.domain;

import java.util.List;
import java.util.Objects;

/**
 * An ordered, cyclic sequence of frames driven by a step counter.
 *
 * <p>The higher-level sugar over a {@code @Tick}-incremented {@code @Reactive State}: instead of
 * doing the index arithmetic by hand on every render — and getting the modulo or the negative-step
 * case wrong — advance one counter per tick and read {@link #frame(long)} inside
 * {@code @Paginated}. The type is platform-neutral; the frames are usually {@link
 * dev.haniel.menu.item.Icon}s but may be any value (lore lines, titles).
 *
 * <p>One frame is shown per step. To slow an animation below the tick rate, divide the step (for
 * example {@code animation.frame(tick / 2)} holds each frame for two ticks); to speed it up, raise
 * the counter by more than one per tick.
 *
 * @param frames the frames in display order; never null or empty
 * @param <T> the frame type, typically {@code Icon}
 */
public record Animation<T>(List<T> frames) {

  public Animation {
    frames = List.copyOf(Objects.requireNonNull(frames, "frames"));
    if (frames.isEmpty()) {
      throw new IllegalArgumentException("an animation needs at least one frame");
    }
  }

  /**
   * Creates an animation from its frames in display order.
   *
   * @param frames the frames; never null or empty
   * @param <T> the frame type
   * @return the animation
   */
  @SafeVarargs
  public static <T> Animation<T> of(T... frames) {
    return new Animation<>(List.of(frames));
  }

  /**
   * Returns the frame shown at the given step, cycling back to the first frame after the last.
   *
   * <p>Any step is valid: it wraps with a floored modulo, so a negative or ever-growing counter
   * still maps onto a frame.
   *
   * @param step the step counter, typically a {@code @Tick}-incremented value
   * @return the frame for this step; never null
   */
  public T frame(long step) {
    return frames.get((int) Math.floorMod(step, frames.size()));
  }

  /**
   * Returns how many frames the animation has.
   *
   * @return the frame count; always at least one
   */
  public int size() {
    return frames.size();
  }
}
