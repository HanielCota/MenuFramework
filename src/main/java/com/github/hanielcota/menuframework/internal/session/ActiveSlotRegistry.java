package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.api.ClickHandler;
import com.github.hanielcota.menuframework.internal.render.RenderResult;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ActiveSlotRegistry {

  private final Int2ObjectMap<ClickHandler> handlers = new Int2ObjectOpenHashMap<>();

  public @Nullable ClickHandler get(int rawSlot) {
    synchronized (handlers) {
      return handlers.get(rawSlot);
    }
  }

  public void replaceWith(@NonNull RenderResult result) {
    java.util.Objects.requireNonNull(result, "result");
    synchronized (handlers) {
      handlers.clear();
      handlers.putAll(result.handlers());
    }
  }

  public void clear() {
    synchronized (handlers) {
      handlers.clear();
    }
  }
}
