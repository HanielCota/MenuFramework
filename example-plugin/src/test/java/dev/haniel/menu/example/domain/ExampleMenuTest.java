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
  void exposesItsPermission() {
    assertEquals("menuexample.open.main", ExampleMenu.MAIN.permission());
  }

  @Test
  void derivesItsResourcePathFromTheId() {
    assertEquals("menus/catalog.yml", ExampleMenu.CATALOG.resourcePath());
  }
}
