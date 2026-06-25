# Templates

## Confirmação

```java
Menu<EmptyMenuState> confirm = Menus.confirmation(
    "Confirmação",
    "Confirmar ação?",
    player -> {
      // executa ação
    },
    player -> {
      // ação de cancelamento opcional
    });
```

## Confirmação com DSL

```java
Menu<ConfirmationState> confirm = MenuBuilder.<ConfirmationState>chest(3, "Confirmação")
    .background(Material.GRAY_STAINED_GLASS_PANE)
    .item("message", ctx -> ItemStacks.named(Material.PAPER, ctx.state().message()))
    .button("confirm", Material.LIME_CONCRETE, click -> {
        this.onConfirm.accept(click.viewer());
        click.close();
    })
    .button("cancel", Material.RED_CONCRETE, MenuInteraction::backOrClose)
    .build();
```

## Configurações com toggles

```java
Menu<SettingsState> settings = MenuBuilder.<SettingsState>chest(3, "Configurações")
    .background(Material.GRAY_STAINED_GLASS_PANE)
    .toggle("sounds", "Sons", SettingsState::sounds, SettingsState::withSounds)
    .toggle("particles", "Partículas", SettingsState::particles, SettingsState::withParticles)
    .toggle("compact", "Modo compacto", SettingsState::compactMode, SettingsState::withCompactMode)
    .closeButton("close")
    .build();
```

## Paginação síncrona

```java
Menu<PaginationComponent.State> menu =
    PaginationComponent.<Product>builder("Produtos", 6)
        .items(products)
        .entryRenderer(product -> ItemStacks.named(product.material(), product.name()))
        .onSelect((product, interaction) -> {
          Player viewer = interaction.viewer();
          viewer.sendMessage("Selecionado: " + product.name());
        })
        .build();
```

## Paginação assíncrona

```java
PaginationLayout layout = PaginationLayout.builder(MenuLayout.chest(6))
    .contentArea(1, 1, 4, 7)
    .previousSlot(5, 0)
    .indicatorSlot(5, 4)
    .nextSlot(5, 8)
    .build();

Menu<AsyncPaginationComponent.State<Product>> menu =
    AsyncPaginationComponent.<Product>builder("products", layout, this::loadPage)
        .entryRenderer(p -> ItemStacks.named(p.material(), p.name()))
        .onSelect((p, interaction) -> interaction.viewer().sendMessage("Selecionado: " + p.name()))
        .build();
```

## Contagem regressiva

```java
Menu<Integer> countdown =
    CountdownComponent.<Integer>builder("Contagem")
        .secondsReader(seconds -> seconds)
        .stateFactory(seconds -> seconds)
        .onFinish(finished -> {})
        .build();
```

## Lista estática em uma região

```java
canvas.component(
    context,
    ListComponent.<State, Warp>builder("warps")
        .entries(ctx -> ctx.state().warps())
        .entryRenderer(warp -> ItemStacks.named(Material.PAPER, warp.name()))
        .onSelect((warp, interaction) -> interaction.viewer().teleport(warp.location()))
        .build());
```
