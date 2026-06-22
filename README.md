# MenuFramework

MenuFramework é uma biblioteca para criar menus de inventário em plugins Paper com estado por sessão, renderização transacional, histórico de navegação, paginação síncrona e assíncrona, tasks pertencentes à sessão e uma fronteira de scheduler compatível com a arquitetura do Folia.

A ideia central é simples: a definição do menu é reutilizável, o estado pertence a uma abertura específica para um jogador, e o framework protege o inventário superior contra mutações acidentais ou interações vanilla que poderiam corromper a UI.

## Sumário

- [Recursos](#recursos)
- [Requisitos](#requisitos)
- [Instalação](#instalação)
- [Uso rápido](#uso-rápido)
- [Conceitos principais](#conceitos-principais)
- [API pública](#api-pública)
- [Renderização e canvas](#renderização-e-canvas)
- [Interações e comandos](#interações-e-comandos)
- [Navegação e histórico](#navegação-e-histórico)
- [Paginação síncrona](#paginação-síncrona)
- [Paginação assíncrona](#paginação-assíncrona)
- [Tasks periódicas](#tasks-periódicas)
- [Tratamento de erros](#tratamento-de-erros)
- [Paper, Folia e threads](#paper-folia-e-threads)
- [Build, testes e formatação](#build-testes-e-formatação)
- [Exemplos](#exemplos)
- [Limites atuais](#limites-atuais)
- [Troubleshooting](#troubleshooting)
- [Documentação adicional](#documentação-adicional)

## Recursos

- Sessões independentes por jogador.
- Identificação segura por `InventoryHolder`, `runtimeId` e `sessionId`, sem depender de título, material, lore ou PDC.
- `MenuFrame` imutável para cada renderização.
- Aplicação diferencial de frames comparando contra o conteúdo real do inventário.
- Rollback de alterações visuais se uma aplicação parcial falhar.
- Estado, frame e revisão publicados como um snapshot consistente.
- Histórico de navegação limitado por jogador.
- Paginação síncrona sobre snapshots em memória.
- Paginação assíncrona com `LOADING`, `READY`, `ERROR`, retry e barreira de geração.
- Tasks assíncronas e periódicas vinculadas à sessão.
- Cancelamento automático de tasks ao fechar, navegar, substituir, desconectar ou desligar.
- Tratamento centralizado de falhas não fatais por `MenuErrorHandler`.
- Arquitetura preparada para Paper/Folia, usando scheduler da entidade para trabalho sensível à região.

## Requisitos

Este repositório atualmente é configurado para:

- Java 25.
- Gradle Wrapper 9.3.0.
- Paper API `26.1.2.build.69-stable`.
- MockBukkit `4.113.2` nos testes.
- JUnit `6.1.0`.
- Google Java Format via Spotless.

O namespace base é:

```text
com.hanielfialho.menuframework
```

A API suportada fica nos packages `com.hanielfialho.menuframework` e `com.hanielfialho.menuframework.api`. Os packages `internal` existem apenas para implementação e não devem ser importados por plugins consumidores.

## Instalação

Após publicação no Maven Central, use a dependência abaixo. Enquanto a primeira versão ainda não estiver disponível, as opções de composite build, subprojeto e Maven Local continuam úteis para desenvolvimento.

### Opção 1: Maven Central

No `build.gradle.kts` do plugin consumidor:

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("io.github.hanielcota:menu-framework:1.0.0")
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.69-stable")
}
```

Garanta que o jar final do seu plugin inclua a biblioteca, por exemplo com shading ou outro mecanismo de distribuição que você já use para dependências internas. A Paper API deve continuar `compileOnly`.

### Opção 2: composite build Gradle

No `settings.gradle.kts` do plugin consumidor:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

includeBuild("../MenuFramework")
```

No `build.gradle.kts` do plugin consumidor:

```kotlin
dependencies {
    implementation("io.github.hanielcota:menu-framework:1.0.0")
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.69-stable")
}
```

Quando usado como composite build, o Gradle substitui essas coordenadas pelo projeto local incluído.

### Opção 3: subprojeto Gradle

Se o plugin e a biblioteca estiverem no mesmo workspace, inclua este projeto como subprojeto.

No `settings.gradle.kts` do workspace:

```kotlin
include(":menu-framework")
project(":menu-framework").projectDir = file("../MenuFramework")
```

No `build.gradle.kts` do plugin:

```kotlin
dependencies {
    implementation(project(":menu-framework"))
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.69-stable")
}
```

### Opção 4: Maven Local

Neste repositório:

```powershell
.\gradlew.bat publishToMavenLocal
```

No plugin consumidor:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("io.github.hanielcota:menu-framework:1.0.0")
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.69-stable")
}
```

### Opção 5: copiar fontes

Também é possível copiar `src/main/java/com/hanielfialho/menuframework` para dentro do plugin. Essa opção é simples, mas você passa a ser responsável por manter os fontes atualizados e formatados.

## Uso rápido

Crie uma única instância do framework quando o plugin estiver habilitado e desligue no `onDisable()`.

```java
import com.hanielfialho.menuframework.MenuFramework;
import com.hanielfialho.menuframework.MenuManager;
import org.bukkit.plugin.java.JavaPlugin;

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

Abra um menu para um jogador:

```java
menus.open(player, counterMenu, CounterMenu.State.initial());
```

O retorno booleano de `open`, `refresh`, `close` e `back` indica se a operação foi aceita para agendamento. O resultado final acontece depois, no scheduler da entidade do jogador.

## Menu mínimo

```java
import com.hanielfialho.menuframework.api.Menu;
import com.hanielfialho.menuframework.api.MenuCanvas;
import com.hanielfialho.menuframework.api.MenuInteraction;
import com.hanielfialho.menuframework.api.MenuLayout;
import com.hanielfialho.menuframework.api.MenuRenderContext;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class CounterMenu implements Menu<CounterMenu.State> {

  private static final MenuLayout LAYOUT = MenuLayout.chest(3);

  @Override
  public MenuLayout layout() {
    return LAYOUT;
  }

  @Override
  public Component title(MenuRenderContext<State> context) {
    return Component.text("Contador");
  }

  @Override
  public void render(MenuRenderContext<State> context, MenuCanvas<State> canvas) {
    State state = context.state();

    canvas.button(
        LAYOUT.slot(1, 4),
        new ItemStack(Material.EMERALD),
        interaction -> interaction.updateState(State::increment));

    canvas.button(
        LAYOUT.slot(2, 4),
        new ItemStack(Material.BARRIER),
        MenuInteraction::close);
  }

  public record State(int clicks) {

    public static State initial() {
      return new State(0);
    }

    public State increment() {
      return new State(this.clicks + 1);
    }
  }
}
```

O menu pode ser singleton. O estado não deve ficar em campos mutáveis do menu, porque a mesma instância pode atender vários jogadores ao mesmo tempo.

## Conceitos principais

### `Menu<S>`

Define uma UI reutilizável. O tipo `S` é o estado da sessão. Uma implementação de menu deve ser stateless ou conter apenas dependências compartilháveis, como serviços, repositórios ou outros menus.

Métodos principais:

- `layout()`: estrutura do inventário. Hoje apenas chest inventories de uma a seis linhas são suportados.
- `interactionPolicy()`: política para interação com inventário inferior. O padrão é `READ_ONLY`.
- `title(context)`: título estrutural avaliado na abertura.
- `render(context, canvas)`: descreve um frame completo.
- `onOpen(context)`: callback após abertura validada.
- `onClose(context, reason)`: callback quando a sessão termina normalmente.

### `MenuSession`

É interno, mas o conceito importa: cada abertura para cada jogador possui `sessionId`, estado, frame, revisão, histórico e tasks próprias.

### `MenuFrame`

Snapshot imutável da renderização. O framework renderiza primeiro, valida o resultado, calcula o diff e só então aplica alterações no inventário.

### Estado

O estado deve ser imutável ou tratado como imutável. Records são uma boa escolha:

```java
public record ProductState(PageCursor cursor, String filter) {
  public ProductState {
    Objects.requireNonNull(cursor, "cursor");
    Objects.requireNonNull(filter, "filter");
  }
}
```

O histórico guarda a referência do estado. Ele não faz deep copy.

## API pública

### `MenuFramework`

Ponto de entrada:

- `MenuFramework.create(plugin)`
- `MenuFramework.create(plugin, configuration)`
- `menus()`
- `configuration()`
- `isShutdown()`
- `shutdown()`

Crie durante `onEnable()`. O construtor falha se o plugin ainda não estiver habilitado.

### `MenuManager`

Fachada para operações por jogador:

- `open(player, menu, initialState)`
- `refresh(player)`
- `close(player)`
- `isOpen(player)`
- `historyDepth(player)`
- `canGoBack(player)`
- `back(player)`

Essas operações são thread-aware pela fronteira de scheduler, mas o código de menu continua devendo respeitar as regras Paper/Folia.

### `MenuFrameworkConfiguration`

Permite configurar profundidade de histórico e handler de erro:

```java
MenuFrameworkConfiguration configuration =
    MenuFrameworkConfiguration.builder()
        .maxNavigationHistoryDepth(64)
        .errorHandler(context -> {
          getLogger().severe(
              "Menu failure: "
                  + context.operation()
                  + " menu="
                  + context.menuTypeName()
                  + " player="
                  + context.viewerId());
          context.cause().printStackTrace();
        })
        .build();

this.menuFramework = MenuFramework.create(this, configuration);
```

O handler pode ser chamado no scheduler da entidade ou em thread assíncrona. Ele deve ser thread-safe e não deve acessar APIs Bukkit dependentes de região.

## Renderização e canvas

`render` deve sempre descrever o frame inteiro. Slots não atribuídos ficam vazios, a menos que você defina um background.

```java
canvas.background(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
canvas.item(10, new ItemStack(Material.PAPER));
canvas.button(13, new ItemStack(Material.DIAMOND), interaction -> {});
canvas.empty(16);
```

Regras importantes:

- Cada slot pode ser atribuído no máximo uma vez por render.
- `AIR` não pode ser usado como ícone. Use `canvas.empty(slot)`.
- `ItemStack` é clonado defensivamente.
- Não altere o inventário Bukkit diretamente dentro de `render`.
- Não bloqueie `render` com banco, HTTP, arquivo ou outra operação lenta.
- O layout não pode mudar enquanto a sessão está aberta.

Use coordenadas quando o código ficar mais legível:

```java
canvas.button(2, 4, new ItemStack(Material.BARRIER), MenuInteraction::close);
```

Isso equivale a `layout().slot(2, 4)`.

## Interações e comandos

Um clique em botão recebe `MenuInteraction<S>`. O evento Bukkit original não é exposto. O framework fornece um snapshot seguro com:

- `sessionId()`
- `viewer()`
- `state()`
- `revision()`
- `historyDepth()`
- `canGoBack()`
- `click()`

Comandos comuns:

```java
interaction.setState(newState);
interaction.updateState(State::increment);
interaction.refresh();
interaction.close();
interaction.open(otherMenu, otherInitialState);
interaction.back();
interaction.backOrClose();
interaction.cancelTask(taskKey);
```

Os comandos são transacionais:

- O buffer só é aplicado se o callback retornar normalmente.
- Se o handler lançar `Exception`, o buffer é descartado e a falha vai ao `MenuErrorHandler`.
- Comandos terminais (`close`, `open`, `back`) não podem ser combinados com mudança de estado ou tasks no mesmo callback.
- Uma interação pode iniciar no máximo uma operação assíncrona.
- Não retenha `MenuInteraction` para usar depois que o handler terminar.

## Política de interação

Por padrão, o framework usa `InteractionPolicy.READ_ONLY`, cancelando cliques e drags em toda a view. Botões do menu ainda são despachados depois do cancelamento do evento.

Para permitir interações restritas ao inventário inferior do jogador:

```java
@Override
public InteractionPolicy interactionPolicy() {
  return InteractionPolicy.PLAYER_INVENTORY_ALLOWED;
}
```

Mesmo com `PLAYER_INVENTORY_ALLOWED`, o inventário superior continua protegido. Shift-click, double-click collect, drags que cruzam o menu e ações desconhecidas continuam bloqueados.

## Navegação e histórico

`interaction.open(...)` abre outro menu e empilha o menu atual no histórico:

```java
interaction.open(productMenu, ProductMenu.State.firstPage());
```

Para voltar:

```java
if (context.canGoBack()) {
  canvas.button(BACK_SLOT, new ItemStack(Material.ARROW), MenuInteraction::back);
}
```

Também há:

```java
interaction.backOrClose();
menus.back(player);
menus.canGoBack(player);
menus.historyDepth(player);
```

Comportamento:

- `menus.open(...)` externo cria uma nova raiz e limpa o histórico do jogador.
- `interaction.open(...)` fecha o menu atual com `MenuCloseReason.NAVIGATION`.
- `back()` fecha o menu atual com `MenuCloseReason.BACK`.
- Se uma abertura de destino falhar, a transição tenta preservar a sessão anterior e o histórico.
- A profundidade padrão é 32 e pode ser configurada.

## Paginação síncrona

Use `Paginator` quando todos os itens já estão disponíveis em memória.

```java
private static final MenuLayout LAYOUT = MenuLayout.chest(6);

private static final PaginationLayout PAGINATION =
    PaginationLayout.builder(LAYOUT)
        .contentArea(1, 1, 4, 7)
        .previousSlot(5, 0)
        .indicatorSlot(5, 4)
        .nextSlot(5, 8)
        .build();

private final Paginator<Product> products;

public ProductMenu(Collection<Product> products) {
  this.products = Paginator.copyOf(products);
}
```

No render:

```java
PageSlice<Product> page = this.products.page(PAGINATION.request(context.state().cursor()));

PAGINATION.forEachEntry(
    page,
    (slot, product, indexInPage, absoluteIndex) ->
        canvas.button(slot, product.icon(), interaction -> select(product)));

PAGINATION.forEachUnusedSlot(page, canvas::empty);

if (page.hasPrevious()) {
  canvas.button(
      PAGINATION.previousSlot(),
      new ItemStack(Material.ARROW),
      interaction -> interaction.updateState(state -> state.withCursor(page.cursor().previous())));
} else {
  canvas.item(PAGINATION.previousSlot(), new ItemStack(Material.GRAY_DYE));
}

if (page.hasNext()) {
  canvas.button(
      PAGINATION.nextSlot(),
      new ItemStack(Material.ARROW),
      interaction -> interaction.updateState(state -> state.withCursor(page.cursor().next())));
} else {
  canvas.item(PAGINATION.nextSlot(), new ItemStack(Material.GRAY_DYE));
}
```

Detalhes:

- O tamanho da página é a quantidade de slots de conteúdo.
- `Paginator` normaliza cursor além da última página para a última página válida.
- Coleções vazias expõem uma página virtual vazia.
- `PageSlice.knownTotal(...)` valida que a janela fornecida é completa.
- `PageSlice.unknownTotal(...)` permite ausência de contagem total, mas exige página cheia quando `hasNext=true`.

## Paginação assíncrona

Use `AsyncPaginator` quando a página vem de banco, HTTP, cache remoto ou qualquer fonte lenta.

```java
private final AsyncPaginator<Product> products =
    AsyncPaginator.create(
        "products",
        (loadContext, request) ->
            CompletableFuture.completedFuture(repository.loadPage(request)));
```

O `PageSource` já é chamado pelo scheduler assíncrono do runtime. Isso significa que uma consulta bloqueante pode ser iniciada ali, desde que ela não acesse objetos Bukkit sensíveis à região.

Estado inicial:

```java
public record State(AsyncPageState<Product> products) {

  public static State initial() {
    return new State(AsyncPageState.initial(PAGINATION.request(PageCursor.FIRST)));
  }

  public State withProducts(AsyncPageState<Product> products) {
    return new State(Objects.requireNonNull(products, "products"));
  }
}
```

Adapter para estado composto:

```java
private static final PageStateAdapter<State, Product> PRODUCT_PAGE =
    new PageStateAdapter<>() {
      @Override
      public AsyncPageState<Product> pageState(State state) {
        return state.products();
      }

      @Override
      public State withPageState(State state, AsyncPageState<Product> pageState) {
        return state.withProducts(pageState);
      }
    };
```

Carregar ao abrir:

```java
@Override
public void onOpen(MenuOpenContext<State> context) {
  this.products.load(context, PRODUCT_PAGE, context.state().products().request());
}
```

Renderize os três estados:

```java
AsyncPageState<Product> pageState = context.state().products();

switch (pageState.status()) {
  case LOADING -> renderLoading(canvas);
  case READY -> renderProducts(canvas, pageState.requirePage());
  case ERROR -> renderError(canvas, pageState.requireError());
}
```

Botões de navegação:

```java
this.products.load(interaction, PRODUCT_PAGE, PAGINATION, page.cursor().next());
this.products.reload(interaction, PRODUCT_PAGE);
```

Por baixo, cada `AsyncPaginator` usa uma `MenuTaskKey`. Uma nova carga com a mesma chave substitui a geração anterior. Se uma resposta antiga chegar depois, ela é descartada porque não corresponde mais à sessão, chave, geração e handle ativos.

Regras para `PageSource`:

- Não capture `Player`, `World`, `Inventory`, `ItemStack` ou outros objetos Bukkit region-bound.
- Retorne DTOs imutáveis ou tratados como imutáveis.
- Retorne `CompletionStage<PageSlice<T>>` não nulo.
- Complete com `PageSlice` não nulo.
- A página retornada deve ter o mesmo `PageRequest` recebido.

## Operações assíncronas diretas

Para casos fora de paginação, use `executeAsync` diretamente:

```java
private static final MenuTaskKey PROFILE_LOAD = MenuTaskKey.of("profile.load");

interaction.executeAsync(
    PROFILE_LOAD,
    taskContext -> CompletableFuture.completedFuture(repository.loadProfile(taskContext.viewerId())),
    (state, generation) -> state.loading(generation),
    (state, generation, profile) -> state.loaded(generation, profile),
    (state, generation, failure) -> state.failed(generation, failure.getMessage()));
```

Fluxo:

1. `onStart` roda no scheduler da entidade e publica o estado de loading.
2. `operation` roda no scheduler assíncrono.
3. `onSuccess` ou `onFailure` volta ao scheduler da entidade.
4. O resultado só é aplicado se a sessão e a geração ainda forem atuais.

`MenuTaskKey` deve seguir o padrão:

```text
[a-z0-9][a-z0-9._-]{0,63}
```

Exemplos válidos: `products`, `profile.load`, `animation_1`, `search-cache`.

## Tasks periódicas

Tasks periódicas rodam no scheduler da entidade. Use para animações curtas, polling leve ou atualização visual barata.

```java
private static final MenuTaskKey ANIMATION = MenuTaskKey.of("animation");

@Override
public void onOpen(MenuOpenContext<State> context) {
  context.repeat(
      ANIMATION,
      MenuTaskSchedule.startingNextTick(4L),
      tick -> MenuTickResult.update(tick.state().nextFrame()));
}
```

Resultados possíveis:

- `MenuTickResult.continueTask()`: não renderiza e mantém a task.
- `MenuTickResult.refresh()`: renderiza o estado atual e mantém a task.
- `MenuTickResult.update(newState)`: troca estado, renderiza e mantém a task.
- `MenuTickResult.stop()`: cancela sem renderizar.
- `MenuTickResult.stopAndRefresh()`: renderiza estado atual e cancela.
- `MenuTickResult.stopWithState(newState)`: troca estado, renderiza e cancela.

Cancelar explicitamente:

```java
interaction.cancelTask(ANIMATION);
```

Tasks são canceladas automaticamente quando a sessão é descartada. O cancelamento é best effort; a garantia real contra resultado atrasado é a validação de sessão, chave, geração e handle.

## Tratamento de erros

Falhas não fatais de callbacks, renders e tasks são encaminhadas ao `MenuErrorHandler`.

```java
MenuFrameworkConfiguration configuration =
    MenuFrameworkConfiguration.builder()
        .errorHandler(
            context ->
                getLogger()
                    .log(
                        Level.SEVERE,
                        DefaultMenuErrorHandler.format(context),
                        context.cause()))
        .build();
```

O contexto contém:

- operação (`MenuFailureOperation`);
- causa original;
- UUID do jogador;
- classe do menu;
- sessão e revisão, quando disponíveis;
- chave, geração e execução de task, quando disponíveis.

Se o handler customizado lançar `Exception`, o framework usa um fallback para o logger do plugin. `Error` não é convertido em falha recuperável comum.

## Paper, Folia e threads

O framework usa:

- `EntityScheduler` do jogador para abertura, renderização, clique, navegação, fechamento e tasks periódicas.
- `AsyncScheduler` para operações assíncronas.
- Retorno ao `EntityScheduler` antes de ler view, alterar estado ou aplicar frame.

Código do consumidor deve seguir as mesmas regras:

- Não acesse `Player`, `World`, `Inventory` ou `ItemStack` compartilhado em trabalho assíncrono.
- Não bloqueie o scheduler da entidade.
- Não guarde callback contexts para usar depois.
- Reagende trabalho Bukkit sensível à região para o contexto correto.

## Build, testes e formatação

Comandos principais:

```powershell
.\gradlew.bat format
.\gradlew.bat test
.\gradlew.bat check
.\gradlew.bat javadoc
.\gradlew.bat build
```

O projeto usa Spotless com Google Java Format. O task `check` depende de:

- `examplesClasses`, para compilar `examples/src/main/java`;
- `spotlessApply`;
- `spotlessCheck`;
- `test`.

Isso faz com que os exemplos também sejam validados pelo build, mesmo não fazendo parte do artifact principal da biblioteca.

Para publicar localmente:

```powershell
.\gradlew.bat publishToMavenLocal
```

## Exemplos

A pasta [examples](examples/) contém:

- `ExamplePlugin`: inicialização e desligamento corretos.
- `CounterMenu`: menu mínimo com estado imutável e navegação.
- `SettingsMenu`: toggles simples e política de interação com inventário inferior.
- `ConfirmationMenu`: confirmação/cancelamento antes de executar uma ação.
- `CountdownMenu`: task periódica que atualiza o estado da sessão.
- `SynchronousProductMenu`: paginação em memória.
- `AsyncProductMenu`: loading, ready, error e retry com `AsyncPaginator`.
- `Product` e `ItemStacks`: objetos auxiliares usados pelos exemplos.

Os exemplos usam o namespace:

```text
com.hanielfialho.menuframework.example
```

Eles são compilados pelo build, mas não são empacotados no source set principal da biblioteca.

## Limites atuais

Esta versão assume:

- Somente chest inventories de uma a seis linhas.
- Título e layout são estruturais durante a sessão.
- Inventário superior sempre pertence ao framework.
- Menus editáveis/transacionais não fazem parte da versão atual.
- Estado de sessão e histórico não recebem deep copy.
- Operações públicas retornam aceite de agendamento, não sucesso final.
- Shutdown não manipula views nem chama `onClose`, por segurança com Folia.
- Cancelamento de futures e scheduled tasks é best effort.
- Várias tasks periódicas no mesmo callback são iniciadas por chave; não há rollback global se uma task posterior for rejeitada.

## Invariantes de produção

1. Não identifique menus pelo título.
2. Não altere inventários diretamente em `render`, handlers, `onOpen` ou `onClose`.
3. Não compartilhe estado mutável entre sessões.
4. Não bloqueie callbacks que rodam no scheduler da entidade.
5. Não capture objetos Bukkit em operações assíncronas.
6. Use `MenuTaskKey` estável para cada operação lógica.
7. Renderize explicitamente estados `LOADING`, `READY` e `ERROR` em paginação assíncrona.
8. Execute smoke tests em Paper e Folia antes de release.
9. Evite `/reload` e hot reload para plugins com scheduler, listeners e estado em memória.

## Troubleshooting

### O menu abre, mas o botão não responde

Verifique se o slot foi registrado com `canvas.button(...)`, não `canvas.item(...)`. O framework só despacha cliques do inventário superior, em slots com botão e click conhecido. Eventos já cancelados por outro plugin antes do listener também não disparam botão.

### Recebi `Slot X was assigned more than once`

O mesmo slot foi escrito duas vezes no mesmo render. Use `if/else` para escolher entre item desabilitado e botão, em vez de chamar `canvas.item` e depois `canvas.button` no mesmo slot.

### Recebi `A menu cannot change its layout while open`

`layout()` retornou uma estrutura diferente durante a mesma sessão. O layout deve ser fixo para a abertura atual. Coloque variações visuais no frame, não no tamanho do inventário.

### O estado assíncrono fica em loading

Confirme que o `CompletionStage` completa, que o `PageSource` não retornou `null`, que a página tem o mesmo `PageRequest` e que `onFailure` retorna um estado não nulo. Se a sessão foi fechada ou substituída, o resultado antigo é descartado de propósito.

### Uma task antiga sobrescreveu um menu novo

Isso não deveria acontecer se a task foi criada pela API. O runtime valida sessão, chave, geração e handle. Se você observa algo parecido, procure estado mutável compartilhado fora do framework.

### `Task key must match [a-z0-9][a-z0-9._-]{0,63}`

A chave da task precisa começar com letra minúscula ou dígito e conter apenas letras minúsculas, dígitos, ponto, underscore ou hífen. Ela deve ter de 1 a 64 caracteres.

### `MenuFramework must be created while the owning plugin is enabled`

Crie o framework em `JavaPlugin#onEnable()`, não em construtor, inicializador estático ou antes do plugin estar habilitado.

### `onClose` não rodou no shutdown

Isso é intencional. `shutdown()` não manipula views nem executa callbacks de menu porque o desligamento do plugin não garante contexto de região seguro no Folia. Libere recursos globais no `onDisable()` do plugin.

## Documentação adicional

- [ARCHITECTURE.md](ARCHITECTURE.md): componentes, invariantes e modelo de threads.
- [PACKAGE_MAP.md](PACKAGE_MAP.md): mapa dos packages e classes.
- [REVIEW.md](REVIEW.md): revisão técnica consolidada, correções e limites conhecidos.
- [PUBLISHING.md](PUBLISHING.md): publicação no Maven Central.
- [examples/README.md](examples/README.md): descrição dos exemplos.
