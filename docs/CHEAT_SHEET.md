# Cheat Sheet

## Canvas

| Método | Descrição |
|--------|-----------|
| `item(slot, icon)` | Item estático |
| `button(slot, icon, handler)` | Botão clicável |
| `empty(slot)` | Slot vazio (não preenchido pelo background) |
| `background(icon)` | Preenche slots não atribuídos |

## Componentes

| Componente | Uso |
|------------|-----|
| `MenuComponents.background()` | Background padrão |
| `MenuComponents.closeButton("slot")` | Botão de fechar |
| `MenuComponents.backButton("slot")` | Botão de voltar |
| `MenuComponents.loadingIndicator("slot")` | Indicador de loading |
| `MenuComponents.retryButton("slot", handler)` | Botão de retry |

## Interações

| Comando | Efeito |
|---------|--------|
| `setState(newState)` | Troca estado e renderiza |
| `updateState(State::increment)` | Atualiza estado e renderiza |
| `refresh()` | Renderiza sem trocar estado |
| `close()` | Fecha o menu |
| `open(menu, state)` | Abre outro menu |
| `back()` | Volta ao menu anterior |
| `backOrClose()` | Volta ou fecha |

## Testes

```java
MenuTestHarness<CounterState> harness =
    MenuTestHarness.create(counter, player, new CounterState(0));

harness.assertItem("counter", Material.EMERALD)
       .assertClickable("counter")
       .click("counter", ClickType.LEFT)
       .assertState(s -> s.clicks() == 1, "clicks should be 1");
```
