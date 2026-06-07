package dev.haniel.menu.example.service;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

/** Sends MiniMessage-formatted example plugin messages. */
public final class MenuMessages {

  private final MiniMessage miniMessage = MiniMessage.miniMessage();

  public void send(CommandSender sender, String message) {
    sender.sendMessage(miniMessage.deserialize(message));
  }
}
