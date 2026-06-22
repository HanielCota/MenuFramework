# Mapa de packages

Todo o projeto utiliza o namespace base `com.hanielfialho.menuframework`.

```text
com.hanielfialho.menuframework
├── API de entrada
├── api
│   ├── error
│   ├── pagination
│   │   └── async
│   └── task
└── internal
    ├── error
    ├── interaction
    ├── inventory
    ├── lifecycle
    ├── platform
    ├── render
    ├── runtime
    ├── session
    └── task
```

## `com.hanielfialho.menuframework`

Pontos de entrada suportados:

- `MenuFramework`: instalação do listener, acesso ao manager e shutdown.
- `MenuManager`: abertura, atualização, fechamento e navegação.
- `MenuFrameworkConfiguration`: configuração imutável do runtime.

## `com.hanielfialho.menuframework.api`

Contratos usados para definir menus:

- `EmptyMenuState`
- `InteractionPolicy`
- `Menu`
- `MenuCanvas`
- `MenuClick`
- `MenuClickHandler`
- `MenuCloseReason`
- `MenuContext`
- `MenuInteraction`
- `MenuLayout`
- `MenuNavigationContext`
- `MenuOpenContext`
- `MenuRenderContext`

## `com.hanielfialho.menuframework.api.error`

- `DefaultMenuErrorHandler`
- `MenuErrorHandler`
- `MenuFailureContext`
- `MenuFailureOperation`

## `com.hanielfialho.menuframework.api.pagination`

- `PageCursor`
- `PageEntryConsumer`
- `PageRequest`
- `PageSlice`
- `PaginationLayout`
- `Paginator`

## `com.hanielfialho.menuframework.api.pagination.async`

- `AsyncPageState`
- `AsyncPaginator`
- `PageLoadContext`
- `PageLoadError`
- `PageLoadStatus`
- `PageSource`
- `PageStateAdapter`

## `com.hanielfialho.menuframework.api.task`

- `MenuAsyncActions`
- `MenuPeriodicTask`
- `MenuTaskActions`
- `MenuTaskContext`
- `MenuTaskKey`
- `MenuTaskSchedule`
- `MenuTickContext`
- `MenuTickResult`

## Runtime interno

Os packages abaixo não fazem parte da API suportada. Alguns tipos são `public` apenas porque o
runtime é dividido em subpackages; aplicações não devem importá-los. Eles são excluídos dos
Javadocs públicos.

### `internal.render`

Canvas, slots, frames, renderer e aplicação diferencial com rollback.

### `internal.inventory`

Holder, identificação de views, listener, proteção de cliques e adaptação de motivos de close.

### `internal.interaction`

Buffer de comandos e despacho transacional de handlers e callbacks de abertura.

### `internal.lifecycle`

Abertura, refresh, fechamento, navegação e histórico por jogador.

### `internal.session`

Sessão tipada, snapshots atômicos e registry concorrente.

### `internal.task`

Operações assíncronas, tarefas periódicas, handles, gerações e cancelamento.

### `internal.platform`

Fronteira com os schedulers de Paper e Folia.

### `internal.error`

Classificação de falhas fatais e encaminhamento ao `MenuErrorHandler`.

### `internal.runtime`

Identidade da instância e coordenação de shutdown.

## Imports comuns

```java
import com.hanielfialho.menuframework.MenuFramework;
import com.hanielfialho.menuframework.MenuManager;
import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuInteraction;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
```
