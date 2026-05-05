package com.github.hanielcota.menuframework.messaging;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/**
 * Service for sending localized and templated messages to players.
 * Replaces hardcoded message strings with centralized message keys.
 */
public interface MessageService {

    /**
     * Sends a message to the player using the given message key.
     *
     * @param player the target player
     * @param key the message key
     * @param args optional formatting arguments
     */
    void send(@NonNull Player player, @NonNull MessageKey key, Object... args);

    /**
     * Builds a message component for the given key without sending it.
     */
    @NonNull Component build(@NonNull MessageKey key, Object... args);
}
