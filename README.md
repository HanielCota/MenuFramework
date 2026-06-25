# MenuFramework

MenuFramework é uma biblioteca para criar menus de inventário em plugins Paper com estado por sessão, renderização transacional, histórico de navegação, paginação síncrona e assíncrona, tasks pertencentes à sessão e uma fronteira de scheduler compatível com a arquitetura do Folia.

A ideia central é simples: a definição do menu é reutilizável, o estado pertence a uma abertura específica para um jogador, e o framework protege o inventário superior contra mutações acidentais ou interações vanilla que poderiam corromper a UI.

## Sumário

- [Recursos](#recursos)
- [Requisitos](#requisitos)
- [Instalação](#instalação)
- [Uso rápido](#uso-rápido)
- [Menu mínimo](#menu-mínimo)
- [Menu mínimo com DSL](#menu-mínimo-com-dsl)
- [Conceitos principais](#conceitos-principais)
- [API pública](#api-pública)
- [Renderização e canvas](#renderização-e-canvas)
- [Layouts nomeados e regiões](#layouts-nomeados-e-regiões)
- [Layouts pré-fabricados](#layouts-pré-fabricados)
- [Componentes, temas e feedback](#componentes-temas-e-feedback)
- [Interações e comandos](#interações-e-comandos)
- [Navegação e histórico](#navegação-e-histórico)
- [Paginação síncrona](#paginação-síncrona)
- [Paginação assíncrona](#paginação-assíncrona)
- [Tasks periódicas](#tasks-periódicas)
- [Modo debug](#modo-debug)
- [Tratamento de erros](#tratamento-de-erros)
- [Paper, Folia e threads](#paper-folia-e-threads)
- [Testando menus](#testando-menus)
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
- Slots nomeados e regiões ordenadas para reduzir números mágicos.
- Componentes reutilizáveis para background, botões, paginação, loading e retry.
- **DSL declarativa** (`MenuBuilder`) para menus comuns sem boilerplate.
- **Componentes de alto nível**: `PaginationComponent`, `AsyncPaginationComponent`, `ToggleButton`, `CountdownComponent`, `ListComponent`.
- Temas de ícones e feedback transacional por sinais.
- Paginação síncrona sobre snapshots em memória.
- Paginação assíncrona com `LOADING`, `READY`, `ERROR`, retry e barreira de geração.
- Tasks assíncronas e periódicas vinculadas à sessão.
- Cancelamento automático de tasks ao fechar, navegar, substituir, desconectar ou desligar.
- Tratamento centralizado de falhas não fatais por `MenuErrorHandler`.
- Arquitetura preparada para Paper/Folia, usando scheduler da entidade para trabalho sensível à região.

## Requisitos

Este repositório atualmente é configurado para:

- Java 25 (toolchain; bytecode continua compatível com JVM 25+).
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

### Opção 1: Maven Central

No `build.gradle.kts` do plugin consumidor:

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
    implementation("io.github.hanielcota:menu-framework:1.0.1")
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.69-stable")
}
```

### Opção 3: subprojeto Gradle

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
    implementation("io.github.hanielcota:menu-framework:1.0.1")
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.69-stable")
}
```

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

## Menu mínimo com DSL

Para menus simples você pode usar `MenuBuilder` e economizar dezenas de linhas:

```java
import com.hanielfialho.menuframework.api.dsl.MenuBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

Menu<CounterMenu.State> counter =
    MenuBuilder.<CounterMenu.State>chest(3, Component.text("Contador"))
        .background(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
        .button(
            "counter",
            ctx -> new ItemStack(Material.EMERALD),
            click -> click.updateState(State::increment))
        .closeButton("close")
        .build();
```

A DSL suporta `item`, `button`, `background`, `closeButton`, `backButton`, `toggle`, `component` e `when`.

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

## API pública

### `MenuFramework`

Ponto de entrada:

- `MenuFramework.create(plugin)`
- `MenuFramework.create(plugin, configuration)`
- `menus()`
- `configuration()`
- `isShutdown()`
- `shutdown()`

### `MenuManager`

Fachada para operações por jogador:

- `open(player, menu, initialState)`
- `refresh(player)`
- `close(player)`
- `isOpen(player)`
- `historyDepth(player)`
- `canGoBack(player)`
- `back(player)`

### `MenuFrameworkConfiguration`

Permite configurar profundidade de histórico, handler de erro, tema, feedback e debug:

```java
MenuFrameworkConfiguration configuration =
    MenuFrameworkConfiguration.builder()
        .maxNavigationHistoryDepth(64)
        .defaultFeedback(SoundMenuFeedback.minecraftDefaults())
        .debug(true)
        .build();
```

## Renderização e canvas

`render` deve sempre descrever o frame inteiro. Slots não atribuídos ficam vazios, a menos que você defina um background.

```java
canvas.background(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
canvas.item(10, new ItemStack(Material.PAPER));
canvas.button(13, new ItemStack(Material.DIAMOND), interaction -> {});
canvas.empty(16);
```

## Layouts nomeados e regiões

```java
private static final MenuLayout LAYOUT =
    MenuLayout.chestBuilder(3)
        .slot("confirm", 1, 2)
        .slot("message", 1, 4)
        .slot("cancel", 1, 6)
        .slot("close", 2, 4)
        .region("content", SlotPatterns.rectangle(0, 1, 0, 7))
        .build();
```

## Layouts pré-fabricados

Para menus comuns, use layouts prontos:

```java
MenuLayout.standardPage(6); // slots: previous, indicator, next
MenuLayout.confirmation();  // slots: confirm, message, cancel
```

## Componentes, temas e feedback

Componentes encapsulam padrões comuns:

```java
canvas.component(context, MenuComponents.background());
canvas.component(context, MenuComponents.closeButton("close"));
canvas.component(
    context,
    MenuButton.<State>at("confirm")
        .icon(confirmIcon)
        .feedback(StandardMenuFeedbackSignals.ACTION_SUCCESS)
        .onClick(interaction -> interaction.close())
        .build());
```

### ToggleButton

```java
canvas.component(
    context,
    ToggleButton.<SettingsState>at("sounds")
        .label("Sons")
        .reader(SettingsState::sounds)
        .writer(SettingsState::withSounds)
        .build());
```

### CountdownComponent

```java
Menu<Integer> countdown =
    CountdownComponent.<Integer>builder("Contagem")
        .secondsReader(seconds -> seconds)
        .stateFactory(seconds -> seconds)
        .onFinish(finished -> {})
        .build();
```

### ListComponent

```java
canvas.component(
    context,
    ListComponent.<State, Warp>builder("warps")
        .entries(ctx -> ctx.state().warps())
        .entryRenderer(warp -> ItemStacks.named(Material.PAPER, warp.name()))
        .onSelect((warp, interaction) -> interaction.viewer().teleport(warp.location()))
        .build());
```

## Interações e comandos

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

## Navegação e histórico

```java
interaction.open(productMenu, ProductMenu.State.firstPage());
interaction.backOrClose();
menus.back(player);
```

## Paginação síncrona

Forma baixo nível com `Paginator` e `PaginationLayout` continua disponível. Para a maioria dos casos, use `PaginationComponent`:

```java
Menu<PaginationComponent.State> menu =
    PaginationComponent.<Product>builder("Produtos", 6)
        .items(products)
        .entryRenderer(product -> product.icon())
        .onSelect((product, interaction) -> select(product))
        .build();

menus.open(player, menu, PaginationComponent.initial());
```

## Paginação assíncrona

Use `AsyncPaginationComponent` para esconder `AsyncPageState` e `PageStateAdapter`:

```java
Menu<AsyncPaginationComponent.State<Product>> menu =
    AsyncPaginationComponent.<Product>builder("products", layout, this::loadPage)
        .entryRenderer(product -> product.icon())
        .onSelect((product, interaction) -> select(product))
        .build();
```

## Tasks periódicas

Veja `CountdownComponent` acima ou use a API de baixo nível:

```java
context.repeat(
    ANIMATION,
    MenuTaskSchedule.startingNextTick(4L),
    tick -> MenuTickResult.update(tick.state().nextFrame()));
```

## Modo debug

Ative para logar transições de lifecycle no logger do plugin:

```java
MenuFrameworkConfiguration.builder().debug(true).build();
```

## Tratamento de erros

Falhas não fatais de callbacks, renders e tasks são encaminhadas ao `MenuErrorHandler`.

## Testando menus

`MenuTestHarness` agora suporta assertions mais ricas, conclusão de operações assíncronas e execução de tasks periódicas:

```java
MenuTestHarness<State> harness =
    MenuTestHarness.create(menu, player, State.initial());

harness.assertItem("counter", Material.EMERALD)
       .assertDisplayName("counter", "Cliques: 0")
       .click("counter", ClickType.LEFT)
       .assertState(s -> s.clicks() == 1, "expected one click")
       .runTicks(5);
```

## Build, testes e formatação

```powershell
.\gradlew.bat format
.\gradlew.bat test
.\gradlew.bat check
.\gradlew.bat javadoc
.\gradlew.bat build
.\gradlew.bat publishToMavenLocal
```

O `check` também executa os testes do source set `examples`.

## Exemplos

A pasta [examples](examples/) contém:

- `ExamplePlugin`: inicialização e desligamento corretos.
- `CounterMenu`: menu mínimo com estado imutável e navegação.
- `SettingsMenu`: toggles simples e política de interação com inventário inferior.
- `ConfirmationMenu`: confirmação/cancelamento antes de executar uma ação.
- `CountdownMenu`: task periódica que atualiza o estado da sessão.
- `SynchronousProductMenu`: paginação em memória.
- `AsyncProductMenu`: loading, ready, error e retry com `AsyncPaginator`.
- `SimpleConfirmationMenu`, `SimpleProductMenu`, `SimpleCountdownMenu`: versões usando as novas APIs de alto nível.

## Documentação adicional

- [docs/QUICK_START.md](docs/QUICK_START.md): 5 minutos de copy-paste.
- [docs/TEMPLATES.md](docs/TEMPLATES.md): templates de menus comuns.
- [docs/CHEAT_SHEET.md](docs/CHEAT_SHEET.md): referência rápida.
- [ARCHITECTURE.md](ARCHITECTURE.md): componentes, invariantes e modelo de threads.
- [PACKAGE_MAP.md](PACKAGE_MAP.md): mapa dos packages e classes.
- [REVIEW.md](REVIEW.md): revisão técnica consolidada.
- [PUBLISHING.md](PUBLISHING.md): publicação no Maven Central.
- [examples/README.md](examples/README.md): descrição dos exemplos.
