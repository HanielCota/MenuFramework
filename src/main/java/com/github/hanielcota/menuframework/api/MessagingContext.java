package com.github.hanielcota.menuframework.api;

import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

/**
 * Messaging and lifecycle actions available during a menu click.
 */
public interface MessagingContext {

    void reply(@NonNull Component message);

    void reply(@NonNull String miniMessage);

    void close();

    void refresh();

    @NonNull Plugin plugin();
}
