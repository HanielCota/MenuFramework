package dev.haniel.menu.compiler.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.haniel.menu.compiler.binding.Instantiator;
import dev.haniel.menu.compiler.binding.UnboundProvider;
import dev.haniel.menu.domain.MaskLayout;
import dev.haniel.menu.domain.MenuId;
import dev.haniel.menu.item.Icon;
import dev.haniel.menu.item.MenuItem;
import dev.haniel.menu.template.MenuTemplate;
import dev.haniel.menu.template.PagedAppearance;
import dev.haniel.menu.template.PagedDecor;
import dev.haniel.menu.template.PagedWiring;
import dev.haniel.menu.template.SlotBinding;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CompiledMenuTest {

  @Test
  void staticMenuExposesIdAndTitle() {
    CompiledStaticMenu<String> menu = staticMenu();

    assertEquals("shop", menu.id().value());
    assertEquals("<green>Shop</green>", menu.title());
  }

  @Test
  void staticMenuDispatchesToVisitStatic() {
    assertEquals("static", staticMenu().accept(new LabelVisitor()));
  }

  @Test
  void pagedMenuDelegatesIdAndTitleToAppearance() {
    CompiledPagedMenu<String> menu = pagedMenu();

    assertEquals("list", menu.id().value());
    assertEquals("<gold>List</gold>", menu.title());
  }

  @Test
  void pagedMenuDispatchesToVisitPaged() {
    assertEquals("paged", pagedMenu().accept(new LabelVisitor()));
  }

  private static CompiledStaticMenu<String> staticMenu() {
    MenuTemplate<String> template = new MenuTemplate<>(new String[9], new SlotBinding[0]);
    return new CompiledStaticMenu<>(new MenuId("shop"), "<green>Shop</green>", template);
  }

  private static CompiledPagedMenu<String> pagedMenu() {
    MaskLayout layout = MaskLayout.resolve(List.of("XXXXXXXXX"), 1);
    PagedAppearance<String> appearance =
        new PagedAppearance<>(
            new MenuId("list"),
            "<gold>List</gold>",
            layout,
            new PagedDecor<>(null, null, "BARRIER"),
            Map.of());
    PagedWiring wiring =
        new PagedWiring(new Instantiator(() -> new Object()), provider(), Map.of(), List.of());
    return new CompiledPagedMenu<>(appearance, wiring);
  }

  private static UnboundProvider provider() {
    try {
      MethodHandle handle =
          MethodHandles.lookup().unreflect(CompiledMenuTest.class.getDeclaredMethod("emptyItems"));
      return new UnboundProvider(handle);
    } catch (NoSuchMethodException | IllegalAccessException exception) {
      throw new IllegalStateException(exception);
    }
  }

  @SuppressWarnings("unused")
  private static List<MenuItem> emptyItems() {
    return List.of(MenuItem.of(Icon.of("STONE")));
  }

  static final class LabelVisitor implements CompiledMenuVisitor<String, String> {

    @Override
    public String visitStatic(CompiledStaticMenu<String> menu) {
      return "static";
    }

    @Override
    public String visitPaged(CompiledPagedMenu<String> menu) {
      return "paged";
    }
  }
}
