package com.github.hanielcota.menuframework.api;

import org.jspecify.annotations.NonNull;

/**
 * Public runtime API for registering menu definitions, opening menus, and managing active menu
 * sessions.
 */
public interface MenuService
    extends MenuDefinitionService,
        MenuTemplateService,
        DynamicMenuContentService,
        MenuOpeningService,
        MenuSessionService,
        MenuDiagnostics {

  /** Returns the menu preloader for background loading of menu content. */
  @NonNull MenuPreloader preloader();
}
