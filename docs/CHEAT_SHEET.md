# Cheat Sheet

## Canvas

| Método | Descrição |
|--------|-----------|
| `item(slot, icon)` | Item estático |
| `button(slot, icon, handler)` | Botão clicável |
| `empty(slot)` | Slot vazio (não preenchido pelo background) |
| `background(icon)` | Preenche slots não atribuídos |

## MenuBuilder DSL

| Método | Descrição |
|--------|-----------|
| `chest(rows, title)` | Inicia construção |
| `background(icon)` | Background padrão |
| `item(slot, icon)` | Item estático |
| `button(slot, icon, handler)` | Botão clicável |
| `closeButton(slot)` | Botão de fechar com tema |
| `backButton(slot)` | Botão de voltar com tema |
| `toggle(slot, label, reader, writer)` | Botão de toggle |
| `component(component)` | Componente reutilizável |
| `when(condition, component)` | Render condicional |

## Componentes

| Componente | Uso |
|------------|-----|
| `MenuComponents.background()` | Background padrão |
| `MenuComponents.closeButton("slot")` | Botão de fechar |
| `MenuComponents.backButton("slot")` | Botão de voltar |
| `MenuComponents.loadingIndicator("slot")` | Indicador de loading |
| `MenuComponents.retryButton("slot", handler)` | Botão de retry |
| `ToggleButton.at("slot")` | Botão de toggle |
| `CountdownComponent.builder("title")` | Contagem regressiva |
| `PaginationComponent.builder("title", rows)` | Paginação síncrona |
| `AsyncPaginationComponent.builder("key", layout, source)` | Paginação assíncrona |
| `ListComponent.builder("region")` | Lista estática em região |

## Layouts pré-fabricados

| Método | Slots disponíveis |
|--------|-------------------|
| `MenuLayout.standardPage(rows)` | `previous`, `indicator`, `next` |
| `MenuLayout.confirmation()` | `confirm`, `message`, `cancel` |

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
       .assertDisplayName("counter", "Cliques: 0")
       .assertClickable("counter")
       .click("counter", ClickType.LEFT)
       .assertState(s -> s.clicks() == 1, "clicks should be 1");

// assíncrono
harness.completeAsync(TASK_KEY, result);
harness.failAsync(TASK_KEY, new RuntimeException("ops"));

// tasks periódicas
harness.runTicks(5);
```
