# MenuFramework — LLM Context Guide

> Este documento fornece contexto arquitetural de alto nível para assistentes de IA trabalhando com a base de código MenuFramework.

---

## Visão Geral do Projeto

MenuFramework é um framework de menus de inventário em **Java 21** voltado para **Paper 1.21.1+**. Ele fornece uma API fluente para desenvolvedores de plugins criarem GUIs estáticas e paginadas, manipulando internamente renderização, ciclo de vida de sessão, despacho de interações e caching.

**Sistema de Build:** Gradle
**Bibliotecas Principais:** Paper API, Caffeine (caching), FastUtil (coleções primitivas), JSpecify (anotações de null-safety)
**Testes:** JUnit 5 + MockBukkit + Mockito

---

## Estrutura de Diretórios

```
src/main/java/com/github/hanielcota/menuframework/
├── api/                        # Interfaces públicas (voltadas para plugins)
│   ├── MenuService.java        # API principal de runtime (composição de todos os serviços)
│   ├── MenuSession.java        # Instância de menu por jogador
│   ├── ClickContext.java       # Contexto de interação passado para handlers
│   ├── ClickHandler.java       # Interface funcional para cliques em slots
│   ├── MenuFeature.java        # Comportamento extensível de menu (sons, refresh, etc.)
│   ├── MenuFeatures.java       # Implementações built-in de features
│   ├── MenuHistory.java        # Interface de histórico de navegação
│   ├── ToggleHandler.java      # Handler para slots toggle
│   └── *Service.java           # Interfaces granulares de serviços (definição, template, abertura, etc.)
│
├── builder/                    # API fluente para construção de menus
│   ├── MenuBuilder.java        # Builder principal (slots, layout, paginação, features)
│   └── MenuRegistrar.java      # Passo final que registra no MenuService
│
├── core/                       # Utilitários compartilhados
│   ├── cache/                  # Construção de cache Caffeine
│   │   └── MenuCacheFactory.java
│   ├── config/                 # Utilitários de validação de configuração
│   │   └── ConfigValidator.java
│   ├── profile/                # Serviço de perfil de jogador (cabeças)
│   │   ├── PlayerProfileService.java
│   │   └── BukkitPlayerProfileService.java
│   ├── server/                 # Camada de abstração do Bukkit
│   │   ├── ServerAccess.java
│   │   └── BukkitServerAccess.java
│   └── text/                   # Utilitários de texto
│       └── MiniMessageProvider.java
│
├── definition/                 # Records de configuração imutáveis
│   ├── MenuDefinition.java     # Especificação completa do menu
│   ├── SlotDefinition.java     # Mapeamento de um slot (template + handler opcional)
│   ├── ItemTemplate.java       # Blueprint de ItemStack
│   ├── PaginationConfig.java   # Configuração de layout de páginas
│   ├── SlotPattern.java        # Padrões de slot predefinidos
│   └── ToggleState.java        # Estado interno de slots toggle
│
├── feature/internal/           # Features built-in
│   ├── RefreshIntervalFeature.java
│   ├── SoundOnOpenFeature.java
│   └── SoundOnClickFeature.java
│
├── interaction/                # Lógica de interação
│   ├── cooldown/
│   │   └── CooldownManager.java
│   ├── feature/
│   │   └── FeatureInvoker.java
│   ├── permission/
│   │   ├── PermissionChecker.java
│   │   └── PermissionFallbackRenderer.java
│   ├── sound/
│   │   └── SoundPlayer.java
│   └── toggle/
│       └── ToggleManager.java
│
├── internal/                   # Detalhes de implementação (plugins NÃO devem usar)
│   ├── DefaultMenuService.java
│   ├── MenuRuntime.java
│   ├── MenuRuntimeFactory.java
│   ├── MenuFrameworkInitializer.java
│   │
│   ├── dispatch/               # Roteamento de eventos
│   │   ├── MenuEventRouter.java
│   │   ├── DefaultMenuEventRouter.java
│   │   └── ClickDispatcher.java
│   ├── event/                  # Listener de eventos Bukkit
│   │   └── MenuListener.java
│   ├── interaction/            # Processamento de cliques
│   │   ├── MenuInteractionController.java
│   │   ├── ClickExecutor.java
│   │   └── InteractionPolicy.java
│   ├── item/                   # Factory de ItemStack
│   │   ├── ItemStackFactory.java
│   │   └── CachedItemStackFactory.java
│   ├── registry/               # Registros em memória
│   │   ├── MenuRegistry.java
│   │   ├── SessionRegistry.java
│   │   ├── DynamicContentRegistry.java
│   │   └── ItemTemplateRegistry.java
│   ├── render/                 # Motor de renderização
│   │   ├── RenderEngine.java
│   │   ├── RenderEngineFactory.java
│   │   ├── StaticRenderStrategy.java
│   │   ├── PaginatedRenderStrategy.java
│   │   ├── SlotRenderer.java
│   │   ├── NavigationRenderer.java
│   │   ├── PageApplier.java
│   │   ├── DynamicContentResolver.java
│   │   └── SlowRenderLogger.java
│   └── session/                # Ciclo de vida de sessão
│       ├── MenuSessionImpl.java
│       ├── MenuSessionImplFactory.java
│       ├── MenuSessionState.java
│       ├── SessionRenderer.java
│       ├── SessionLifecycle.java
│       ├── RefreshScheduler.java
│       ├── SessionFactory.java
│       ├── ClickContextImpl.java
│       ├── PlayerResolver.java
│       ├── PlayerMenuHistory.java
│       ├── ActiveSlotRegistry.java
│       ├── SessionCommands.java
│       ├── SessionQuery.java
│       └── InteractiveMenuSession.java
│
├── messaging/                  # Serviço de mensagens
│   ├── MessageService.java
│   ├── DefaultMessageService.java
│   └── MessageKey.java
│
├── pagination/                 # Motor de paginação
│   ├── PaginationEngine.java
│   ├── PaginationEngineFactory.java
│   ├── PageView.java
│   └── PageCacheKey.java
│
└── scheduler/                  # Abstração de agendamento
    ├── SchedulerAdapter.java
    └── PaperSchedulerAdapter.java
```

---

## Padrões de Design Principais

### 1. **Fluent Builder Pattern**
`MenuBuilder` fornece configuração encadeável. Ele coleta definições de slots, bindings de layout, configuração de paginação e features, depois produz um `MenuRegistrar` que registra a `MenuDefinition` final no `MenuService`.

### 2. **Strategy Pattern**
Interface `RenderStrategy` com duas implementações:
- `StaticRenderStrategy` — renderiza apenas slots fixos
- `PaginatedRenderStrategy` — manipula conteúdo dinâmico + paginação + navegação

O `RenderEngine` seleciona a estratégia apropriada com base se paginação está configurada.

### 3. **Registry Pattern**
Todos os dados são armazenados em registros tipados:
- `MenuRegistry` — definições de menus + templates de itens + providers de conteúdo dinâmico
- `SessionRegistry` — sessões ativas de jogadores (UUID → MenuSession)
- `DynamicContentRegistry` — itens de conteúdo dinâmico e providers por ID de menu

### 4. **Factory Pattern**
Múltiplas factories ligam dependências:
- `MenuRuntimeFactory` — constrói todo o runtime interno
- `SessionFactory` — cria sessões assincronamente na main thread
- `MenuSessionImplFactory` — monta componentes de sessão (estado, renderizador, lifecycle, interações)
- `RenderEngineFactory` — cria o motor de renderização com estratégias apropriadas

### 5. **Session State Pattern**
`MenuSessionImpl` delega para componentes especializados:
- `MenuSessionState` — mantém ID do visualizador, definição, página atual, flag de disposed
- `SessionRenderer` — dispara re-renderização
- `MenuInteractionController` — roteia cliques para handlers
- `SessionLifecycle` — manipula disposal, fechamento de inventário, callbacks de features

---

## Modelo de Threading

**Regra de Ouro:** Todas as chamadas à API do Bukkit acontecem na main thread.

- `MenuService#open()` retorna um `CompletableFuture<MenuSession>`
- `SessionFactory#create()` agenda criação de sessão via `SchedulerAdapter.runSync()`
- `SessionLifecycle#dispose()` também roda na main thread
- `RefreshScheduler` usa `runSyncRepeating()` para ticks de features
- A abstração `SchedulerAdapter` permite testes com schedulers síncronos

**Operações de cache** (Caffeine) são thread-safe e podem acontecer off-thread. Apenas mutação de inventário é main-threaded.

---

## Estratégia de Caching

Três níveis de caching:

1. **ItemStack Cache** (`CachedItemStackFactory`)
   - Chaves: hash de `ItemTemplate`
   - Valor: `ItemStack` base (clonado defensivamente antes de colocação no inventário)

2. **Page Cache** (`PaginationEngine`)
   - Chaves: `PageCacheKey` = `(menuId, pageNumber, contentHash)`
   - Valor: `PageView` contendo layout computado `ItemStack[]`
   - Hash de conteúdo muda quando conteúdo dinâmico é atualizado

3. **Session Cache** (`SessionRegistry`)
   - Chaves: `UUID` (jogador)
   - Valor: `MenuSessionImpl`
   - Cacheado para lookups rápidos durante eventos de clique

---

## Convenções de Null-Safety

- Todos os parâmetros e retornos de API pública usam `@NonNull` por padrão
- `@Nullable` é explícito para valores opcionais
- Records validam non-null em construtores compactos
- `Objects.requireNonNull()` usado para verificações defensivas
- Métodos internos podem retornar `@Nullable` quando resolução pode falhar (ex: `resolvePlayer()`, `resolveSession()`)

---

## Gotchas Importantes para Assistentes de IA

### MenuBuilder#build() sempre usa título Component.empty()
O builder NÃO expõe um método `.title()` atualmente, apesar de `MenuDefinition` suportar títulos. Para definir um título, você deve criar a `MenuDefinition` manualmente ou estender o builder.

### SessionLifecycle constructor aceita @Nullable MenuSession
`MenuSessionImplFactory` passa `null` inicialmente, depois chama `setSession()` depois que o objeto de sessão é criado. Isso é intencional para quebrar a dependência circular.

### MenuRuntime tem registros de dual-purpose
`MenuRegistry` implementa múltiplas interfaces:
- `MenuRegistry` (definições)
- `ItemTemplateRegistry` (templates)
- `DynamicContentRegistry` (conteúdo dinâmico)

`MenuRuntime` os expõe através de métodos semânticos (`definitions()`, `templates()`, `dynamicContent()`) mas todos delegam para a mesma instância de `menuRegistry`.

### ClickContextImpl é um record
`ClickContextImpl` é um `record` Java. Records auto-geram acessores. Acesse campos diretamente: `ctx.player()`, `ctx.session()`. Não há Lombok no projeto.

### Slots de conteúdo de paginação são índices na lista de itens dinâmicos, não slots de inventário
`contentSlots` em `PaginationConfig` define quais **slots de inventário** contêm itens paginados. A lista de itens dinâmicos é fatiada por página, e cada item é colocado sequencialmente naqueles slots.

### Assinatura de DynamicContentProvider provide()
```java
@NonNull List<SlotDefinition> provide(@NonNull Player player, @NonNull MenuSession session)
```
Ambos os parâmetros são garantidos non-null quando chamados de `DynamicContentResolver`. O resolver faz fallback para conteúdo estático se jogador ou sessão não puderem ser resolvidos.

### ToggleManager persiste estado em MenuSessionState
Quando um toggle slot é clicado, `ToggleManager` atualiza o estado no `MenuSessionState.toggleStates()` (um `ConcurrentHashMap`). Isso garante que o estado persista entre re-renders. O `SessionRenderer` reaplica esses estados após cada refresh.

### SlotDefinition factory methods usam método privado `slot()`
Para eliminar repetição de boilerplate, os factory methods (`of()`, `navigational()`, `withCooldown()`, etc.) delegam para um método privado `slot()` que constrói o record. Isso mantém o código DRY sem expor o método público.

### PaginationEngine é compartilhado entre MenuRegistry e RenderEngine
`MenuRuntimeFactory` cria UMA única instância de `PaginationEngine` e a passa tanto para `MenuRegistry` quanto para `RenderEngineFactory`. Isso garante que invalidações de cache feitas via `MenuRegistry.invalidate()` afetem o cache usado pela renderização.

### PlayerResolver NÃO fecha sessões automaticamente
Após a refatoração, `PlayerResolver` é uma classe pura que apenas resolve jogadores. O fechamento de sessões para jogadores offline é responsabilidade do caller (ex: `MenuInteractionController`, `SessionRenderer`, `RefreshScheduler`).

---

## Convenções de Teste

- Testes unitários usam **MockBukkit** para mockar API do Bukkit
- **Mockito** para mocking de dependências (MenuSession, ClickContext, etc.)
- Classe de plugin de teste: `MenuTestPlugin`
- Testes cobrem: validação de builder, validação de config, lógica de paginação, lifecycle de sessão, callbacks de features, tratamento de erros, interações, permissões, cooldowns, toggle, histórico
- **Total: 112 testes** (54 adicionados na última atualização)
- SpotBugs e PMD enforce qualidade de código em CI

---

## Exemplos Práticos

### Criar um Menu Simples
```java
MenuService menus = MenuFramework.create(plugin);
MenuFramework.builder("shop", menus)
    .rows(3)
    .slot(13, ItemTemplate.builder(Material.DIAMOND).name("Buy").build(),
        ctx -> ctx.reply("Clicked!"))
    .register();
```

### Abrir um Menu para um Jogador
```java
menus.open(player, "shop"); // CompletableFuture<MenuSession>
```

### Adicionar Conteúdo Dinâmico
```java
menus.setDynamicContentProvider("leaderboard", (player, session) -> {
    return List.of(SlotDefinition.of(-1, template, handler));
});
```

### Fechar Todas as Sessões no Disable do Plugin
```java
menus.shutdown(); // ou MenuFramework.shutdown() para singleton
```

---

## Boas Práticas

### 1. **Sempre use ItemTemplate.builder() para criar itens**
Em vez de criar `ItemStack` manualmente, use o builder fluente do framework. Isso garante caching automático e consistência.

```java
// ❌ Ruim
ItemStack item = new ItemStack(Material.DIAMOND);
ItemMeta meta = item.getItemMeta();
meta.setDisplayName("Diamond");
item.setItemMeta(meta);

// ✅ Bom
ItemTemplate template = ItemTemplate.builder(Material.DIAMOND)
    .name("Diamond")
    .lore("Line 1", "Line 2")
    .glow(true)
    .build();
```

### 2. **Reutilize ItemTemplates**
Templates são imutáveis e cacheados. Crie-os uma vez e reutilize em múltiplos slots ou menus.

```java
private static final ItemTemplate CLOSE_BUTTON = ItemTemplate.builder(Material.BARRIER)
    .name("<red>Close")
    .build();

// Use em múltiplos menus
builder.slot(0, CLOSE_BUTTON, ctx -> ctx.session().close());
```

### 3. **Use ClickHandler com cuidado em conteúdo dinâmico**
Handlers de clique em conteúdo dinâmico são chamados frequentemente. Mantenha a lógica leve e delegue trabalho pesado para threads assíncronas.

```java
// ❌ Ruim - trabalho pesado na main thread
menus.setDynamicContentProvider("shop", (player, session) -> {
    List<ItemStack> items = database.loadAllItems(); // DB call na main thread!
    // ...
});

// ✅ Bom - pré-carregue dados
private List<SlotDefinition> cachedItems;

menus.setDynamicContentProvider("shop", (player, session) -> {
    return cachedItems; // Dados já carregados
});
```

### 4. **Sempre feche menus ao desabilitar plugin**
```java
@Override
public void onDisable() {
    MenuFramework.shutdown(); // Fecha todas as sessões e limpa recursos
}
```

### 5. **Use MenuFeatures para comportamento reutilizável**
```java
public class AutoRefreshFeature implements MenuFeature {
    @Override
    public void onTick(MenuSession session) {
        // Atualiza a cada tick configurado
        session.refresh();
    }
}
```

### 6. **Valide dados de entrada**
```java
MenuDefinition definition = MenuDefinition.builder()
    .id("shop")
    .title(MiniMessage.miniMessage().deserialize("<green>Shop"))
    .rows(3)
    .slots(slotDefinitions)
    .build();

// O framework valida automaticamente, mas verifique suas configs
ConfigValidator.validate(definition);
```

### 7. **Use paginação corretamente**
```java
PaginationConfig pagination = PaginationConfig.builder()
    .contentSlots(IntStream.range(10, 16).toArray()) // Slots 10-15 para conteúdo
    .previousPageSlot(18)
    .nextPageSlot(26)
    .navigationTemplate(ItemTemplate.builder(Material.ARROW).name("Navigation").build())
    .build();

builder.pagination(pagination);
```

---

## Anti-Patterns

### 1. **Não acesse classes internal/ diretamente**
```java
// ❌ NUNCA faça isso
MenuRuntime runtime = ((DefaultMenuService) menus).getRuntime();
```

### 2. **Não crie ItemStack na mão**
```java
// ❌ Evite
ItemStack item = new ItemStack(Material.STONE);
item.editMeta(meta -> meta.displayName(Component.text("Stone")));
builder.slot(0, item); // Não existe - use ItemTemplate
```

### 3. **Não faça I/O em handlers de clique**
```java
// ❌ Ruim
builder.slot(0, template, ctx -> {
    database.save(ctx.player()); // Bloqueia main thread
});

// ✅ Bom
builder.slot(0, template, ctx -> {
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        database.save(ctx.player());
    });
});
```

### 4. **Não ignore CompletableFuture**
```java
// ❌ Ruim - exceções silenciadas
menus.open(player, "shop");

// ✅ Bom - trate erros
menus.open(player, "shop")
    .thenAccept(session -> player.sendMessage("Menu opened!"))
    .exceptionally(throwable -> {
        player.sendMessage("Failed to open menu!");
        return null;
    });
```

### 5. **Não modifique o inventário diretamente**
```java
// ❌ NUNCA
Inventory inv = session.getInventory();
inv.setItem(0, new ItemStack(Material.STONE)); // Quebra o estado interno

// ✅ Use a API do framework
session.refresh(); // Re-renderiza corretamente
```

---

## Troubleshooting

### Sessão não abre
- Verifique se o menu foi registrado: `menus.isRegistered("shop")`
- Verifique se o jogador tem permissão
- Verifique logs para erros de `SessionFactory`

### Itens paginados não aparecem
- Verifique se `contentSlots` está correto (0-53 para inventário de 6 linhas)
- Verifique se `DynamicContentProvider` retorna itens
- Verifique se o cache foi invalidado após mudança de dados

### Memory leaks
- Sempre chame `menus.shutdown()` no onDisable
- Verifique se sessões estão sendo fechadas: `menus.getActiveSessions()`
- Use o `SlowRenderLogger` para identificar renderizações lentas

### Cliques não funcionam
- Verifique se o handler não é null: `SlotDefinition.of(slot, template, handler)`
- Verifique se `InteractionPolicy` permite o clique
- Verifique se a sessão não foi disposed

---

## Exemplos Avançados

### Menu com Confirmação
```java
public class ConfirmMenu {
    public static void open(Player player, String action, Runnable onConfirm) {
        MenuFramework.builder("confirm", menus)
            .rows(3)
            .slot(11, ItemTemplate.builder(Material.LIME_WOOL)
                .name("<green>Confirm").build(),
                ctx -> {
                    onConfirm.run();
                    ctx.session().close();
                })
            .slot(15, ItemTemplate.builder(Material.RED_WOOL)
                .name("<red>Cancel").build(),
                ctx -> ctx.session().close())
            .feature(MenuFeatures.sound(Sound.BLOCK_NOTE_BLOCK_PLING))
            .register();
        
        menus.open(player, "confirm");
    }
}
```

### Menu Paginado de Leaderboard
```java
public class LeaderboardMenu {
    private final List<SlotDefinition> entries = new ArrayList<>();
    
    public void updateEntries(List<PlayerStats> stats) {
        entries.clear();
        for (int i = 0; i < stats.size(); i++) {
            PlayerStats stat = stats.get(i);
            ItemTemplate template = ItemTemplate.builder(Material.PLAYER_HEAD)
                .name("<yellow>" + (i + 1) + ". " + stat.getName())
                .lore("<gray>Kills: " + stat.getKills())
                .build();
            
            entries.add(SlotDefinition.of(-1, template));
        }
    }
    
    public void register() {
        menus.setDynamicContentProvider("leaderboard", (player, session) -> entries);
        
        PaginationConfig pagination = PaginationConfig.builder()
            .contentSlots(IntStream.range(10, 35).filter(i -> i % 9 != 0 && i % 9 != 8).toArray())
            .previousPageSlot(45)
            .nextPageSlot(53)
            .build();
        
        MenuFramework.builder("leaderboard", menus)
            .rows(6)
            .pagination(pagination)
            .feature(MenuFeatures.refresh(20)) // Atualiza a cada 1 segundo
            .register();
    }
}
```

### Feature Personalizada
```java
public record ParticleFeature(Particle particle, int interval) implements MenuFeature {
    private int tickCounter = 0;
    
    @Override
    public void onTick(MenuSession session) {
        if (++tickCounter >= interval) {
            tickCounter = 0;
            Player player = session.viewer();
            player.getWorld().spawnParticle(
                particle,
                player.getLocation().add(0, 2, 0),
                1
            );
        }
    }
    
    @Override
    public void onOpen(MenuSession session) {
        session.viewer().sendMessage("Particles ativadas!");
    }
}

// Uso:
builder.feature(new ParticleFeature(Particle.HEART, 20));
```

---

## Testes

### Teste de Builder
```java
@Test
void shouldBuildMenuDefinition() {
    MenuDefinition definition = MenuDefinition.builder()
        .id("test")
        .rows(3)
        .build();
    
    assertEquals("test", definition.id());
    assertEquals(27, definition.size());
}
```

### Teste de Paginação
```java
@Test
void shouldCalculatePages() {
    List<SlotDefinition> items = IntStream.range(0, 100)
        .mapToObj(i -> SlotDefinition.of(-1, template))
        .toList();
    
    PageView view = paginationEngine.render(items, 0, config);
    
    assertEquals(10, view.totalPages()); // 100 items / 10 per page
}
```

### Teste de Sessão com MockBukkit
```java
@Test
void shouldOpenMenu() {
    PlayerMock player = server.addPlayer();
    
    menus.open(player, "shop").join();
    
    assertTrue(player.getOpenInventory().getTitle().contains("Shop"));
}
```

---

## Performance

### Otimizações
1. **ItemStack Cache**: Templates idênticos reutilizam ItemStacks cacheados
2. **Page Cache**: Mudanças de página usam cache quando conteúdo não mudou
3. **Lazy Loading**: Conteúdo dinâmico é resolvido apenas quando necessário
4. **Batch Rendering**: Slots são renderizados em batch para minimizar chamadas de inventário

### Métricas
- Use `SlowRenderLogger` para identificar menus lentos
- Monitore o tamanho do `SessionRegistry`
- Verifique hit rate do Caffeine cache

---

## Debugging

### Habilitar Logs
```java
// Em sua implementação de SchedulerAdapter ou similar
Logger logger = Logger.getLogger("MenuFramework");
logger.setLevel(Level.FINE);
```

### Inspecionar Estado
```java
// Ver sessões ativas
Collection<MenuSession> sessions = menus.getActiveSessions();
System.out.println("Active sessions: " + sessions.size());

// Ver definições registradas
Collection<String> menuIds = menus.getRegisteredMenuIds();
System.out.println("Registered menus: " + menuIds);
```

### Debug de Renderização
```java
// Adicione um listener para eventos de renderização
// (requer implementação customizada de RenderEngine)
renderEngine.setDebugMode(true);
```

---

## Adicionando Novas Features

Para adicionar uma nova feature de menu:

1. Crie uma classe implementando `MenuFeature`
2. Implemente hooks de lifecycle: `onOpen()`, `onClose()`, `onTick()`
3. Adicione um factory method em `MenuFeatures.java`
4. Registre a feature via `MenuBuilder#feature()`

Exemplo:
```java
public record CustomFeature(String data) implements MenuFeature {
    @Override
    public void onOpen(MenuSession session) {
        // Lógica quando menu abre
    }
}
```

---

## Tarefas Comuns

### Criar um menu simples
```java
MenuService menus = MenuFramework.create(plugin);
MenuFramework.builder("shop", menus)
    .rows(3)
    .slot(13, ItemTemplate.builder(Material.DIAMOND).name("Buy").build(),
        ctx -> ctx.reply("Clicked!"))
    .register();
```

### Abrir um menu para um jogador
```java
menus.open(player, "shop"); // CompletableFuture<MenuSession>
```

### Adicionar conteúdo dinâmico
```java
menus.setDynamicContentProvider("leaderboard", (player, session) -> {
    return List.of(SlotDefinition.of(-1, template, handler));
});
```

### Fechar todas as sessões no disable do plugin
```java
menus.shutdown(); // ou MenuFramework.shutdown() para singleton
```

---

## Grafo de Dependências (Conceitual)

```
Plugin Code
    ↓ (usa)
MenuFramework (ponto de entrada)
    ↓ (cria)
MenuService (API pública)
    ↓ (delega para)
MenuRuntime (wiring interno)
    ↓ (mantém)
Registries + RenderEngine + SessionFactory + EventRouter
    ↓ (cria)
MenuSessionImpl
    ↓ (composto de)
State + Renderer + InteractionController + Lifecycle
```

---

## Novas Features Implementadas

### 1. **MenuHistory - Navegação entre Menus**

O framework agora rastreia automaticamente o histórico de menus abertos por cada jogador.

```java
// Abrir um menu (adiciona o menu atual ao histórico automaticamente)
ctx.open("settings");

// Voltar ao menu anterior
ctx.back();

// Verificar se há menu anterior
if (ctx.hasPreviousMenu()) {
    ctx.reply("<gray>Pressione ESC para voltar");
}
```

### 2. **Cooldown por Slot e Global**

Evite spam de cliques com cooldowns configuráveis.

```java
// Cooldown global de 20 ticks (1 segundo) em um slot específico
builder.slotWithCooldown(13, template, handler, 20);

// Cooldown padrão global de 100ms é aplicado automaticamente
// Cooldown por slot é adicional ao global
```

### 3. **Atualização Parcial de Slots**

Atualize slots individuais sem re-renderizar toda a página.

```java
MenuSession session = menus.open(player, "shop").join();

// Atualizar um slot
session.updateSlot(13, ItemTemplate.builder(Material.EMERALD).name("Novo Item").build());

// Atualizar múltiplos slots
session.updateSlots(Map.of(
    10, template1,
    11, template2,
    12, template3
));
```

### 4. **Fill Patterns**

Preencha slots automaticamente com padrões predefinidos.

```java
// Preencher bordas
builder.fillBorder(ItemTemplate.builder(Material.GRAY_STAINED_GLASS_PANE).build());

// Preencher slots vazios
builder.fillEmpty(ItemTemplate.builder(Material.BLACK_STAINED_GLASS_PANE).build());

// Padrões predefinidos
builder.fillPattern(MenuBuilder.SlotPattern.CHECKERBOARD, template1);
builder.fillPattern(MenuBuilder.SlotPattern.CORNERS, template2);
builder.fillPattern(MenuBuilder.SlotPattern.TOP_ROW, template3);
```

### 5. **Validação de Permissão em Slots**

Controle o acesso a slots baseado em permissões.

```java
// Slot que requer permissão
builder.slotWithPermission(
    13,
    adminTemplate,
    ctx -> ctx.reply("<green>Acesso concedido!"),
    "menuframework.admin",
    noPermissionTemplate // Item mostrado se não tiver permissão
);
```

### 6. **Toggle/Checkbox Slots**

Slots com estado booleano que alternam ao clicar e persistem entre re-renders.

```java
builder.toggleSlot(
    15,
    ItemTemplate.builder(Material.LIME_WOOL).name("<green>Ativado").build(),
    ItemTemplate.builder(Material.RED_WOOL).name("<red>Desativado").build(),
    true, // Estado inicial
    (ctx, enabled) -> {
        plugin.getConfig().set("auto-save", enabled);
        ctx.reply("Auto-save: " + (enabled ? "<green>ON" : "<red>OFF"));
    }
);
```

### 7. **Player Inventory Integration**

Permita interação com o inventário do jogador enquanto o menu está aberto.

```java
builder
    .allowPlayerInventoryClicks(true)
    .allowShiftClick(true)
    .onPlayerInventoryClick((player, clickType, slot, session) -> {
        player.sendMessage("Você clicou no slot " + slot + " do seu inventário!");
    });
```

### 8. **Título e Linhas no Builder**

Configure título e tamanho do menu diretamente no builder.

```java
MenuFramework.builder("shop", menus)
    .title("<green>Loja de Itens")
    .rows(5) // 5 linhas = 45 slots
    .slot(13, buyTemplate, ctx -> { /* ... */ })
    .register();
```

---

## Changelog

### 2026-05-05
- Documento inicial criado

### 2026-05-05 (Atualização 1)
- Adicionado MenuHistory (navegação entre menus)
- Adicionado cooldown por slot e global
- Adicionado atualização parcial de slots
- Adicionado fill patterns (border, empty, patterns)
- Adicionado validação de permissão em slots
- Adicionado toggle/checkbox slots
- Adicionado player inventory integration
- Melhorado MenuBuilder com title() e rows()

### 2026-05-05 (Atualização 2 - Major Refactor)
- **Removido Lombok**: Todo o código agora é Java 21 puro sem dependência de Lombok
- **Restruturação de pacotes**: Utilitários movidos de `internal/` para `core/`
  - `internal/cache/` → `core/cache/`
  - `internal/config/` → `core/config/`
  - `internal/server/` → `core/server/`
  - `internal/text/` → `core/text/`
- **Classes convertidas para records**: `BukkitServerAccess`, `BukkitPlayerProfileService`, `DefaultMessageService`
- **Modernização de logs**: Todos os logs convertidos para `String.formatted()` com avaliação lazy
- **Correções críticas de bugs**:
  - DynamicContentResolver agora resolve sessão corretamente via `menuService.getSession()`
  - PaginationEngine é compartilhado entre registry e renderização
  - Double dispose agora completa future corretamente
  - Toggle state persiste entre re-renders via `MenuSessionState.toggleStates()`
  - ActiveSlotRegistry armazena `SlotDefinition` completo (não só handlers)
  - SessionFactory só registra sessão após `fireOpenFeatures()`
  - CooldownManager registra slot cooldown mesmo quando global cooldown ativo
- **Melhorias de qualidade**:
  - `MenuDefinition`: String formatting com `.formatted()`
  - `SlotDefinition`: Método factory privado `slot()` para eliminar repetição
  - `CachedItemStackFactory`: Variável `key` extraída em `applyPdc()`
  - `ItemTemplate`: `equals`, `hashCode` e `toString` customizados para comparar conteúdo de arrays
  - `PageView`: `equals` usa `ItemStack.isSimilar()` para comparação correta
- **Auditoria de código**:
  - Removidos imports não utilizados
  - Removidos parâmetros não utilizados
  - Inlined métodos de delegação pura
  - Removidos pacotes vazios
- **Testes**: Adicionados 54 novos testes unitários
  - CooldownManager (6), PermissionChecker (4), ToggleManager (6)
  - PlayerMenuHistory (7), ConfigValidator (13)
  - SlotDefinition (10), PageView (9)

---

*Última atualização: 2026-05-05*
