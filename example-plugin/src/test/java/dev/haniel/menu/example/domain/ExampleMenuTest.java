package dev.haniel.menu.example.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.haniel.menu.domain.MenuId;
import org.junit.jupiter.api.Test;

class ExampleMenuTest {

  @Test
  void exposesItsStronglyTypedId() {
    assertEquals(new MenuId("catalog"), ExampleMenu.CATALOG.id());
  }

  @Test
  void exposesMenuPermissionsAsAnnotationConstants() {
    assertEquals("menuexample.open.main", ExampleMenu.MAIN_PERMISSION);
    assertEquals("menuexample.open.catalog", ExampleMenu.CATALOG_PERMISSION);
    assertEquals(ExampleMenu.MAIN_PERMISSION, ExampleMenu.MAIN.permission());
    assertEquals(ExampleMenu.CATALOG_PERMISSION, ExampleMenu.CATALOG.permission());
  }

  @Test
  void derivesItsResourcePathFromTheId() {
    assertEquals("menus/catalog.yml", ExampleMenu.CATALOG.resourcePath());
  }
}
