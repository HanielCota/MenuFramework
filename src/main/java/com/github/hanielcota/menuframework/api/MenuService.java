package com.github.hanielcota.menuframework.api;

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
        MenuDiagnostics {}
