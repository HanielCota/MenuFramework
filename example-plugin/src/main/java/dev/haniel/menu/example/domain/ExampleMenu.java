package dev.haniel.menu.example.domain;

import dev.haniel.menu.domain.MenuId;

/** Strongly typed menu ids used by the example plugin. */
public enum ExampleMenu {
  MAIN("main", "menuexample.open.main"),
  CATALOG("catalog", "menuexample.open.catalog");

  public static final String MAIN_PERMISSION = "menuexample.open.main";
  public static final String CATALOG_PERMISSION = "menuexample.open.catalog";

  private final MenuId id;
  private final String permission;

  ExampleMenu(String id, String permission) {
    this.id = new MenuId(id);
    this.permission = permission;
  }

  public MenuId id() {
    return id;
  }

  public String permission() {
    return permission;
  }

  public String resourcePath() {
    return "menus/" + id.value() + ".yml";
  }
}
