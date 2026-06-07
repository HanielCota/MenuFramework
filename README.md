# MenuFramework

Annotation-driven inventory menu framework for Paper and Folia plugins.

Menu behavior lives in Java classes. Menu appearance lives in YAML files under
`plugins/<PluginName>/menus`. The framework compiles both at boot and can reload YAML at runtime.

## Modules

- `menu-core`: platform-free annotations, config, compiler, state and merge logic.
- `menu-paper`: Paper-facing facade, listener, rendering and registry.
- `menu-folia`: Folia scheduler implementation.
- `example-plugin`: runnable example plugin using the public API.

## Static Menu

```java
@Menu(id = "main")
public final class MainMenu {

  @Button(id = "open-catalog")
  public void openCatalog(MenuClick click) {
    click.message("<green>Opening catalog.</green>");
  }
}
```

```yaml
title: "<green>Main Menu</green>"
rows: 3
buttons:
  open-catalog:
    slot: 13
    material: CHEST
    name: "<green>Open Catalog</green>"
```

## Paginated Reactive Menu

```java
@Menu(id = "catalog")
public final class CatalogMenu {

  @Reactive private final State<Category> category = State.of(Category.TOOLS);

  @Button(id = "next-category")
  public void nextCategory() {
    category.set(category.get().next());
  }

  @Paginated
  public List<MenuItem> products() {
    return products.in(category.get()).stream().map(this::item).toList();
  }
}
```

The pagination mask uses:

- `X`: content slot
- `#`: border slot
- `<`: previous page
- `>`: next page
- space: empty slot

## Bootstrapping

```java
MenuFramework framework =
    MenuFramework.builder(this)
        .scan("com.example.plugin.menu")
        .build();
```

Use a custom instantiator when menus have constructor dependencies:

```java
MenuFramework.builder(this)
    .instantiator(type -> container.create(type))
    .scan("com.example.plugin.menu")
    .build();
```

## Reload

Synchronous compatibility API:

```java
boolean reloaded = framework.reload(new MenuId("main"));
int count = framework.reloadAll();
```

Detailed report API:

```java
ReloadReport report = framework.reloadAllReport();
```

Async YAML reload:

```java
framework.reloadAllReportAsync()
    .thenAccept(report -> sender.sendMessage(Component.text(report.successCount() + " reloaded")));
```

Only YAML IO and parsing run asynchronously. Menu compilation and `ItemStack` creation run on the
plugin scheduler because Bukkit inventory APIs are not thread-safe.

YAML reloads are cached by file metadata plus a CRC32 content checksum, so unchanged files are not
parsed again while timestamp edge cases still invalidate correctly.

## Validation

The loader validates platform-independent YAML errors early:

- `rows` must be between `1` and `6`
- button slots must fit the menu size
- pagination masks must match the menu rows and be 9 columns wide
- pagination masks must contain at least one `X`

Material names are validated in the Paper renderer with Bukkit's `Material` registry.

## Build

```powershell
.\gradlew.bat clean test build
```

The example plugin shadow jar is built at:

```text
example-plugin/build/libs/example-plugin-0.1.0-SNAPSHOT.jar
```
