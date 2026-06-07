package dev.haniel.menu.example.command;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import dev.haniel.menu.example.service.MenuCommandService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

class MenuExampleCommandTest {

  private final MenuCommandService service = mock(MenuCommandService.class);
  private final MenuExampleCommand command = new MenuExampleCommand(service);

  @Test
  void delegatesToServiceAndReturnsHandled() {
    CommandSender sender = mock(CommandSender.class);
    Command bukkitCommand = mock(Command.class);
    String[] args = {"catalog"};

    boolean handled = command.onCommand(sender, bukkitCommand, "menuexample", args);

    assertTrue(handled);
    verify(service).execute(sender, args);
  }
}
