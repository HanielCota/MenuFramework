# Quick Start

## Instalação

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("io.github.hanielcota:menu-framework:1.0.1")
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.69-stable")
}
```

## Inicialização

```java
public final class MyPlugin extends JavaPlugin {

  private MenuFramework menuFramework;

  @Override
  public void onEnable() {
    this.menuFramework = MenuFramework.create(this);
  }

  @Override
  public void onDisable() {
    if (this.menuFramework != null) {
      this.menuFramework.shutdown();
    }
  }

  public MenuManager menus() {
    return this.menuFramework.menus();
  }
}
```

## Menu mínimo com DSL

```java
Menu<CounterState> counter = MenuBuilder.<CounterState>chest(3, "Contador")
    .background(Material.GRAY_STAINED_GLASS_PANE)
    .button("counter",
        ctx -> ItemStacks.named(Material.EMERALD, "Cliques: " + ctx.state().clicks()),
        click -> click.updateState(s -> new CounterState(s.clicks() + 1)))
    .closeButton("close")
    .build();

menus.open(player, counter, new CounterState(0));
```

## Confirmação

```java
Menu<EmptyMenuState> confirm = Menus.confirmation(
    "Confirmação",
    "Tem certeza?",
    player -> player.sendMessage("Confirmado!"));
```

## Paginação síncrona

```java
Menu<PaginationComponent.State> products =
    PaginationComponent.<Product>builder("Produtos", 6)
        .items(productList)
        .entryRenderer(product -> ItemStacks.named(product.material(), product.name()))
        .onSelect((product, interaction) -> interaction.viewer().sendMessage(product.name()))
        .build();

menus.open(player, products, PaginationComponent.initial());
```

Leia mais em [TEMPLATES.md](TEMPLATES.md) e [CHEAT_SHEET.md](CHEAT_SHEET.md).
