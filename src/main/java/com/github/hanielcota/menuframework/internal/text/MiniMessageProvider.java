package com.github.hanielcota.menuframework.internal.text;

import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.NonNull;

public final class MiniMessageProvider {

  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

  private MiniMessageProvider() {}

  public static @NonNull Component deserialize(@NonNull String input) {
    return MINI_MESSAGE.deserialize(Objects.requireNonNull(input, "input"));
  }
}
