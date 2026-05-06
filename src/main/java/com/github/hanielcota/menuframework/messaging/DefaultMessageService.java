package com.github.hanielcota.menuframework.messaging;

import java.text.MessageFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/** Simple implementation of {@link MessageService} using default messages. */
public final class DefaultMessageService implements MessageService {

  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

  @Override
  public void send(@NonNull Player player, @NonNull MessageKey key, Object... args) {
    player.sendMessage(build(key, args));
  }

  @Override
  public @NonNull Component build(@NonNull MessageKey key, Object... args) {
    String formatted;
    try {
      formatted = MessageFormat.format(key.defaultMessage(), args);
    } catch (IllegalArgumentException e) {
      formatted = key.defaultMessage() + " [format error: " + e.getMessage() + "]";
    }
    return MINI_MESSAGE.deserialize("<red>" + formatted);
  }
}
