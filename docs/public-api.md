# Public API Boundary

`dev.haniel.menu.paper.MenuFramework` is the default application-facing API.

Plugin boot code should use the facade to build the framework, scan menu packages, manually
register exceptional instances, open menus and reload YAML. The facade owns the normal wiring:
platform scheduler detection, ClassGraph discovery, registry construction and the single Bukkit
listener registration.

The lower-level registry, discovery, compiler, scheduler and listener types remain public for
advanced integrations and source compatibility. They are not required for ordinary plugin code, and
new application code should treat them as extension points rather than the primary surface.

One `MenuFramework` instance per plugin is assumed.
