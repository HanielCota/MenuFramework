/**
 * The curated entry point for writing menus — start here.
 *
 * <p>A menu is a plain class plus a YAML file. Behaviour lives in the class (annotated methods);
 * appearance (slot, material, name, lore) lives in {@code menus/<id>.yml} and hot-reloads. The
 * surface a menu author touches:
 *
 * <ul>
 *   <li><b>Annotations</b> — {@link dev.haniel.menu.annotation.Menu @Menu} on the class,
 *       {@link dev.haniel.menu.annotation.Button @Button} on a click handler,
 *       {@link dev.haniel.menu.annotation.Paginated @Paginated} on the content supplier, and
 *       {@link dev.haniel.menu.annotation.Reactive @Reactive} on a {@link dev.haniel.menu.state.State}
 *       field that re-renders the menu when it changes.
 *   <li><b>Click handling</b> — a {@code @Button} method may take no parameter, a
 *       {@link org.bukkit.entity.Player}, a {@link dev.haniel.menu.paper.api.MenuClick}, or a
 *       {@link dev.haniel.menu.click.ClickContext}. {@code MenuClick} is the convenient one:
 *       {@code player()}, {@code message(...)} and {@code close()}.
 *   <li><b>Content</b> — build paginated entries with {@link dev.haniel.menu.item.MenuItem} and
 *       {@link dev.haniel.menu.paper.api.Icons} ({@code Icons.of(Material)} for compiler-checked
 *       materials).
 *   <li><b>Bootstrap</b> — wire everything from {@code onEnable} with
 *       {@link dev.haniel.menu.paper.MenuFramework#builder(org.bukkit.plugin.java.JavaPlugin)},
 *       then {@code scan(...)} the package holding the menus.
 * </ul>
 *
 * <p>Adding a menu is just a new annotated class plus its YAML: discovery registers it without any
 * change to {@code onEnable}.
 */
package dev.haniel.menu.paper.api;
