package com.github.hanielcota.menuframework.api;

import org.jspecify.annotations.NonNull;

/**
 * Navigation actions available during a menu click.
 */
public interface NavigationContext {

    void open(@NonNull String menuId);

    void back();

    boolean hasPreviousMenu();

    void setPage(int page);

    int currentPage();
}
