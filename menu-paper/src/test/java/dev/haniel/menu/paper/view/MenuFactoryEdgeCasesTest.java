package dev.haniel.menu.paper.view;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.binding.UnboundProvider;
import dev.haniel.menu.compiler.model.CompiledMenu;
import dev.haniel.menu.compiler.model.CompiledPagedMenu;
import dev.haniel.menu.compiler.model.CompiledStaticMenu;
import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.paper.render.InventoryFactory;
import dev.haniel.menu.scheduler.MenuScheduler;
import dev.haniel.menu.template.IconFactory;
import dev.haniel.menu.template.MenuTemplate;
import dev.haniel.menu.template.PagedAppearance;
import dev.haniel.menu.template.PagedDecor;
import dev.haniel.menu.template.PagedWiring;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

/**
 * Visitor-dispatch probes for {@link MenuFactory}: a {@link CompiledStaticMenu} must build a {@link
 * StaticPaperMenu} (deserializing its title once through the runtime), and a {@link
 * CompiledPagedMenu} must build a {@link ReactivePagedMenu} without touching any runtime services
 * at build time (binding is deferred to open).
 */
class MenuFactoryEdgeCasesTest {

  @Test
  void staticCompiledMenuBuildsAStaticPaperMenu() {
    MiniMessage miniMessage = mock(MiniMessage.class);
    when(miniMessage.deserialize("<b>Shop</b>")).thenReturn(Component.text("Shop"));
    MenuRuntime runtime = runtimeWith(miniMessage);
    CompiledMenu<ItemStack> compiled =
        new CompiledStaticMenu<>(new MenuId("shop"), "<b>Shop</b>", emptyTemplate());

    PaperMenu menu = new MenuFactory(runtime).create(compiled);

    assertInstanceOf(
        StaticPaperMenu.class, menu, "a static compiled menu must build a static view");
    verify(miniMessage).deserialize("<b>Shop</b>");
  }

  @Test
  void pagedCompiledMenuBuildsAReactivePagedMenu() {
    CompiledMenu<ItemStack> compiled = pagedMenu();

    PaperMenu menu = new MenuFactory(runtimeWith(mock(MiniMessage.class))).create(compiled);

    assertInstanceOf(
        ReactivePagedMenu.class, menu, "a paged compiled menu must build a reactive view");
  }

  @Test
  void pagedBuildDefersAllBindingAndTouchesNoRuntimeServices() {
    // visitPaged only stores the plan + runtime; nothing should be deserialized, scheduled or
    // rendered until a player actually opens the menu.
    MiniMessage miniMessage = mock(MiniMessage.class);
    MenuScheduler scheduler = mock(MenuScheduler.class);
    InventoryFactory inventories = mock(InventoryFactory.class);
    MenuRuntime runtime =
        new MenuRuntime(
            Logger.getLogger("factory-test"), icons(), miniMessage, scheduler, inventories);

    new MenuFactory(runtime).create(pagedMenu());

    verifyNoInteractions(miniMessage, scheduler, inventories);
  }

  @Test
  void createDelegatesToAcceptForVisitorDispatch() {
    // The factory must dispatch through accept(), not instanceof: a compiled menu whose accept
    // routes to visitStatic yields a static view regardless of declared type.
    MiniMessage miniMessage = mock(MiniMessage.class);
    when(miniMessage.deserialize("t")).thenReturn(Component.text("t"));
    MenuFactory factory = new MenuFactory(runtimeWith(miniMessage));
    CompiledStaticMenu<ItemStack> compiled =
        new CompiledStaticMenu<>(new MenuId("a"), "t", emptyTemplate());

    PaperMenu viaCreate = factory.create(compiled);
    PaperMenu viaAccept = compiled.accept(factory);

    assertSame(viaCreate.getClass(), viaAccept.getClass(), "create() and accept() must agree");
  }

  private static MenuRuntime runtimeWith(MiniMessage miniMessage) {
    return new MenuRuntime(
        Logger.getLogger("factory-test"),
        icons(),
        miniMessage,
        mock(MenuScheduler.class),
        mock(InventoryFactory.class));
  }

  private static IconFactory<ItemStack> icons() {
    return icon -> mock(ItemStack.class);
  }

  private static MenuTemplate<ItemStack> emptyTemplate() {
    return new MenuTemplate<>(new ItemStack[9], new dev.haniel.menu.template.SlotBinding[0]);
  }

  private static CompiledMenu<ItemStack> pagedMenu() {
    PagedAppearance<ItemStack> appearance =
        new PagedAppearance<>(
            new MenuId("paged"),
            "<title>",
            MaskLayout.resolve(List.of("<X>      "), 1),
            new PagedDecor<>(null, null, null),
            Map.of());
    PagedWiring wiring =
        new PagedWiring(new Instantiator(Object::new), provider(), Map.of(), List.of());
    return new CompiledPagedMenu<>(appearance, wiring);
  }

  private static UnboundProvider provider() {
    try {
      return new UnboundProvider(
          MethodHandles.lookup()
              .findStatic(
                  MenuFactoryEdgeCasesTest.class,
                  "noItems",
                  java.lang.invoke.MethodType.methodType(List.class, Object.class)));
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(error);
    }
  }

  @SuppressWarnings("unused") // bound reflectively as the paginated provider; never invoked here
  private static List<MenuItem> noItems(Object instance) {
    return List.of();
  }
}
