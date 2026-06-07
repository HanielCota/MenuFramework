package dev.haniel.menu.paper.view;

import dev.haniel.menu.compiler.model.CompiledMenu;
import dev.haniel.menu.compiler.model.CompiledMenuVisitor;
import dev.haniel.menu.compiler.model.CompiledPagedMenu;
import dev.haniel.menu.compiler.model.CompiledStaticMenu;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

/**
 * Turns a {@link CompiledMenu} into an openable {@link PaperMenu}, dispatching over the two
 * compiled variants. Static menus are shared and pre-rendered; paginated menus are reactive.
 */
public final class MenuFactory implements CompiledMenuVisitor<ItemStack, PaperMenu> {

  private final MenuRuntime runtime;

  /**
   * Creates a factory backed by the platform runtime.
   *
   * @param runtime the platform services; never null
   */
  public MenuFactory(MenuRuntime runtime) {
    this.runtime = runtime;
  }

  /**
   * Builds the openable menu for the given compiled result.
   *
   * @param compiled the compiled static or paginated menu; never null
   * @return the openable menu
   */
  public PaperMenu create(CompiledMenu<ItemStack> compiled) {
    return compiled.accept(this);
  }

  @Override
  public PaperMenu visitStatic(CompiledStaticMenu<ItemStack> compiled) {
    Component title = runtime.miniMessage().deserialize(compiled.title());
    return new StaticPaperMenu(new MenuView(title, compiled.template()));
  }

  @Override
  public PaperMenu visitPaged(CompiledPagedMenu<ItemStack> compiled) {
    return new ReactivePagedMenu(compiled, runtime);
  }
}
