package com.github.hanielcota.menuframework.internal.session;

import com.github.hanielcota.menuframework.definition.SlotDefinition;
import com.github.hanielcota.menuframework.internal.render.RenderResult;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ActiveSlotRegistry {

  private final Int2ObjectMap<SlotDefinition> slots = new Int2ObjectOpenHashMap<>();

  public @Nullable SlotDefinition get(int rawSlot) {
    synchronized (slots) {
      return slots.get(rawSlot);
    }
  }

  public void replaceWith(@NonNull RenderResult result) {
    java.util.Objects.requireNonNull(result, "result");
    synchronized (slots) {
      slots.clear();
      slots.putAll(result.slots());
    }
  }

  public void clear() {
    synchronized (slots) {
      slots.clear();
    }
  }
}
