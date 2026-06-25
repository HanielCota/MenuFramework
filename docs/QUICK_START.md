# Quick Start

## Instalação

```kotlin
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
Menu<CounterState> counter = MenuBuilder.<CounterState>chest(3, Component.text("Contador"))
    .background(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
    .button("counter",
        ctx -> ItemStacks.named(Material.EMERALD, "Cliques: " + ctx.state().clicks()),
        click -> click.updateState(s -> new CounterState(s.clicks() + 1)))
    .closeButton("close")
    .build();
```

## Abrir

```java
menus.open(player, counter, new CounterState(0));
```

Leia mais em [TEMPLATES.md](TEMPLATES.md) e [CHEAT_SHEET.md](CHEAT_SHEET.md).
