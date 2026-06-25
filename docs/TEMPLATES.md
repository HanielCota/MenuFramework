# Templates

## Confirmação

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

## Paginação síncrona

```java
Paginator<Product> paginator = Paginator.copyOf(products);

Menu<PageCursor> menu = MenuBuilder.<PageCursor>chest(6, "Produtos")
    .background(Material.GRAY_STAINED_GLASS_PANE)
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
