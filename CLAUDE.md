# Guia de Desenvolvimento — Plugins Java PaperMC ☕✨

Diretrizes para qualquer plugin Java desenvolvido para **PaperMC**. O objetivo é código limpo, legível, coeso e fácil de manter, combinando técnicas de **Clean Code** com as **9 regras de Object Calisthenics**.

> **Stack alvo:** Java 25 · Paper 26 (Minecraft 1.21+) · Brigadier · Adventure API · Gradle.

---

## Princípios Gerais

- Legibilidade imediata acima de tudo. Código limpo é autoexplicativo.
- **Princípio do Escoteiro:** deixe o código mais limpo do que encontrou.
- Prefira composição a herança.
- Bootstrap (`onEnable`/`onDisable`) deve ser mínimo: apenas registrar comandos, listeners e serviços.
- Sem lógica de negócio dentro de `Listener` ou `Command` — delegue para `service`.

---

## 1. Extração de Variáveis Explícitas

Não deixe expressões complexas perdidas dentro de `if`/`while`. Extraia para variáveis com nomes que explicam o contexto.

### 🛑 Ruim
```java
if (player.getLevel() >= 10 && player.hasPermission("plugin.use") && !player.isDead()) {
    // ...
}
```

### ✅ Bom
```java
boolean hasRequiredLevel = player.getLevel() >= 10;
boolean canUseFeature = player.hasPermission("plugin.use") && !player.isDead();

if (hasRequiredLevel && canUseFeature) {
    // ...
}
```

---

## 2. Extração de Métodos Pequenos (Single Responsibility)

Cada método faz **uma única coisa bem feita**. Métodos com no máximo ~15 linhas.

### 🛑 Ruim
```java
public void handleJoin(Player player) {
    if (player == null || !player.isOnline()) {
        return;
    }
    PlayerData data = repository.findById(player.getUniqueId()).orElse(PlayerData.empty());
    data.incrementJoins();
    repository.save(data);
    player.sendMessage(Component.text("Bem-vindo de volta!"));
}
```

### ✅ Bom
```java
public void handleJoin(Player player) {
    if (!isValid(player)) {
        return;
    }
    registerJoin(player.getUniqueId());
    sendWelcome(player);
}

private void registerJoin(UUID playerId) {
    PlayerData data = repository.findById(playerId).orElse(PlayerData.empty());
    data.incrementJoins();
    repository.save(data);
}
```

---

## 3. Cláusulas de Guarda e Retorno Precoce

Valide condições de parada primeiro e saia cedo. **Evite `else`** — quase sempre indica lógica acoplada.

### 🛑 Ruim
```java
public void execute(CommandSender sender) {
    if (sender instanceof Player) {
        handle((Player) sender);
    } else {
        sender.sendMessage("Apenas jogadores!");
    }
}
```

### ✅ Bom
```java
public void execute(CommandSender sender) {
    if (!(sender instanceof Player player)) {
        sender.sendMessage(Component.text("Apenas jogadores!"));
        return;
    }
    handle(player);
}
```

---

## 4. Um Nível de Indentação por Método

Cada método deve ter **no máximo 1 nível de indentação**. Aninhamento profundo → extraia método.

### 🛑 Ruim
```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    if (event.getPlayer().hasPermission("plugin.vip")) {
        for (String reward : rewards) {
            if (!reward.isEmpty()) {
                event.getPlayer().sendMessage(reward);
            }
        }
    }
}
```

### ✅ Bom
```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    if (isVip(player)) {
        sendRewards(player);
    }
}

private void sendRewards(Player player) {
    rewards.stream()
        .filter(reward -> !reward.isEmpty())
        .forEach(player::sendMessage);
}
```

---

## 5. Stream API e Lambdas

Use Java moderno para eliminar `for`/`if` repetitivos em coleções.

### 🛑 Ruim
```java
List<String> names = new ArrayList<>();
for (Player player : players) {
    if (player.isOnline() && player.hasPermission("plugin.admin")) {
        names.add(player.getName());
    }
}
```

### ✅ Bom
```java
List<String> adminNames = players.stream()
    .filter(Player::isOnline)
    .filter(player -> player.hasPermission("plugin.admin"))
    .map(Player::getName)
    .toList();
```

> Exceção a "um ponto por linha": Fluent APIs próprias e builders (ex.: `Component.text(...).color(...)` do Adventure) são aceitáveis.

---

## 6. Optional em vez de null

Nunca retorne `null`. Use `Optional` para deixar explícito que o valor pode não existir.

### 🛑 Ruim
```java
public PlayerData findData(UUID id) {
    return storage.get(id); // pode ser null
}
```

### ✅ Bom
```java
public Optional<PlayerData> findData(UUID id) {
    return Optional.ofNullable(storage.get(id));
}

// Uso:
findData(playerId)
    .map(PlayerData::name)
    .ifPresent(player::sendMessage);
```

---

## 7. Records para Dados (Java 14+)

Para DTOs, value objects e configs imutáveis, use `record` em vez de classes cheias de boilerplate.

### 🛑 Ruim
```java
public class CoinAmount {
    private final int value;
    public CoinAmount(int value) { this.value = value; }
    public int getValue() { return value; }
    // + equals, hashCode, toString...
}
```

### ✅ Bom
```java
public record CoinAmount(int value) {
    public CoinAmount {
        if (value < 0) throw new IllegalArgumentException("Coins não podem ser negativos");
    }
}
```

---

## 8. Envolva Primitivos e Strings (Wrap Primitives)

Crie value objects para conceitos do domínio. Evita *primitive obsession* e erros de tipo.

### 🛑 Ruim
```java
public void transfer(UUID from, UUID to, int amount) { ... }
```

### ✅ Bom
```java
public record PlayerId(UUID value) {
    public PlayerId { Objects.requireNonNull(value); }
}

public void transfer(PlayerId from, PlayerId to, CoinAmount amount) { ... }
```

---

## 9. Coleções de Primeira Classe

Classe que contém uma coleção **não deve ter outros campos**. Encapsule coleções com comportamento. **Nunca exponha a coleção interna** — retorne cópias imutáveis para que ninguém de fora dê `.add()`/`.clear()` no estado interno.

### ✅ Bom
```java
public final class ArenaRegistry {
    private final Map<String, Arena> arenas = new HashMap<>();

    public void register(Arena arena) {
        arenas.put(arena.name(), arena);
    }

    public Optional<Arena> findByName(String name) {
        return Optional.ofNullable(arenas.get(name));
    }

    // .toList() (Java 16+) já gera lista imutável — o mapa interno fica protegido
    public List<Arena> findAvailable() {
        return arenas.values().stream()
            .filter(Arena::isAvailable)
            .toList();
    }

    // Para retornar a coleção inteira, blinde a cópia:
    public List<Arena> all() {
        return List.copyOf(arenas.values()); // ou Collections.unmodifiableList(...)
    }
}
```

---

## 10. Tell, Don't Ask

Diga ao objeto o que fazer, não pergunte o estado para decidir fora dele.

### 🛑 Ruim
```java
if (account.getBalance() >= price) {
    account.setBalance(account.getBalance() - price);
}
```

### ✅ Bom
```java
account.withdraw(price); // o objeto cuida do próprio estado e valida

// Na classe Account:
public void withdraw(CoinAmount price) {
    if (balance.isLessThan(price)) {
        throw new InsufficientFundsException();
    }
    this.balance = balance.subtract(price);
}
```

> Getters são aceitáveis em DTOs de persistência e records de configuração.

---

## 11. Tratamento de Exceções Limpo

Exceções devem ser **específicas e informativas**. Nunca silencie um erro com `catch` vazio e evite capturar `Exception` genérica — esconde bugs que crasham o servidor ou corrompem dados.

### 🛑 Ruim
```java
try {
    player.teleport(target.getSpawnLocation());
} catch (Exception e) {
    // silêncio... se falhar, ninguém sabe o motivo
}
```

### ✅ Bom
```java
try {
    player.teleport(target.getSpawnLocation());
} catch (IllegalStateException e) {
    logger.warning("Falha ao teleportar " + player.getName() + ": destino não carregado.");
    player.sendMessage(Component.text("Carregando, tente novamente.", NamedTextColor.RED));
}
```

> Prefira lançar exceções de domínio (`InsufficientFundsException`, `ArenaNotFoundException`) em vez de erros genéricos.

---

## 12. Evite Strings Mágicas (Chaves Fortemente Tipadas)

Strings soltas para caminhos de `config.yml` ou chaves de `PersistentDataContainer` (NBT) causam falhas silenciosas por typos. Centralize em constantes ou Enums.

### 🛑 Ruim
```java
player.getPersistentDataContainer()
    .set(new NamespacedKey(plugin, "owner_id"), PersistentDataType.STRING, uuid.toString());
```

### ✅ Bom
```java
public enum DataKey {
    OWNER_ID("owner_id"),
    REGION_SIZE("region_size");

    private final String key;

    DataKey(String key) {
        this.key = key;
    }

    public NamespacedKey namespaced(Plugin plugin) {
        return new NamespacedKey(plugin, key);
    }
}

// Uso seguro, com auto-complete da IDE:
player.getPersistentDataContainer()
    .set(DataKey.OWNER_ID.namespaced(plugin), PersistentDataType.STRING, uuid.toString());
```

---

## 13. Generics

Generics dão segurança de tipo em tempo de compilação e eliminam casts. Em frameworks (comandos, argumentos, repositórios) são essenciais.

### Nunca use raw types
```java
// 🛑 Ruim — raw type, perde checagem e gera warning
List arenas = new ArrayList();

// ✅ Bom
List<Arena> arenas = new ArrayList<>();
```

### Interfaces genéricas para abstrações reutilizáveis

Repositórios e parsers se beneficiam de um parâmetro de tipo em vez de duplicar a interface por entidade.

```java
/**
 * Generic CRUD repository keyed by an identifier.
 *
 * @param <ID> the identifier type
 * @param <T>  the stored entity type
 */
public interface Repository<ID, T> {
    Optional<T> findById(ID id);
    void save(T entity);
    void delete(ID id);
}

public interface PlayerRepository extends Repository<PlayerId, PlayerData> {}
```

```java
/**
 * Parses a command argument into a typed value.
 *
 * @param <T> the resolved argument type
 */
public interface ArgumentType<T> {
    T parse(String raw) throws ArgumentParseException;
}
```

### PECS — Producer Extends, Consumer Super

Use `? extends` quando a coleção **produz** valores (você lê) e `? super` quando ela **consome** (você escreve).

```java
// Produtor: lê de qualquer subtipo de Reward
public void grantAll(List<? extends Reward> rewards, Player player) {
    rewards.forEach(reward -> reward.grant(player));
}

// Consumidor: escreve em qualquer supertipo de Arena
public void collectInto(Collection<? super Arena> target) {
    target.addAll(arenas);
}
```

### Métodos genéricos em vez de `Object`
```java
// 🛑 Ruim — perde o tipo, exige cast no chamador
public Object firstOrNull(List list) { ... }

// ✅ Bom — preserva o tipo
public <T> Optional<T> first(List<T> list) {
    return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
}
```

### Sem casts inseguros

Evite `(T)` solto. Se for inevitável (ex.: ponte com API antiga), isole o cast em um único ponto com `@SuppressWarnings("unchecked")` justificado por comentário — nunca espalhe pelo código.

> Para chaves heterogêneas com tipo seguro (ex.: registry de serviços por classe), use o padrão *typesafe heterogeneous container*: `Map<Class<T>, T>` com `Class<T>` como chave.

---

| Elemento                            | Limite      |
|-------------------------------------|-------------|
| Método                              | ~15 linhas  |
| Classe                              | ~150 linhas |
| Variáveis de instância por classe   | máx. 2–3    |
| Classes por pacote                  | ~10         |
| Nível de indentação por método      | 1           |

Mais campos que o limite → use composição (agrupe dependências relacionadas em um objeto).

---

## Nomenclatura

- **Não abrevie.** `int daysSinceLastLogin`, não `int d`.
- Nomes descritivos e completos: `Location spawnLocation`, não `Loc loc`.
- Classes `final` por padrão; abra para herança só quando intencional.

---

## Formatação (Google Java Format)

O código segue o **Google Java Style**, aplicado automaticamente pelo `google-java-format`. **O formatador é a fonte da verdade** — nunca formate na mão brigando com a ferramenta. Rode antes de cada commit.

Convenções principais:
- Indentação de **2 espaços** (nunca tabs).
- Limite de **100 colunas** por linha.
- Imports ordenados, **sem wildcards** (`import java.util.*;` proibido).
- Chave de abertura na mesma linha; sem linhas em branco duplicadas.

Integre via **Spotless** no Gradle para checar/aplicar no build:
```kotlin
plugins {
    id("com.diffplug.spotless") version "6.25.0"
}

spotless {
    java {
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
```

- `./gradlew spotlessApply` → formata tudo.
- `./gradlew spotlessCheck` → falha o build se algo estiver fora do padrão (roda junto de `build`).

> Os exemplos deste guia usam 4 espaços por legibilidade no Markdown; no projeto real vale **sempre** a saída do `google-java-format` (2 espaços).

---

## Estrutura de Pacotes (referência)

```
com.example.plugin/
├── command/      # Executors / Brigadier
├── listener/     # EventListeners
├── domain/       # Entidades, Value Objects, Enums
├── repository/   # Acesso a dados (YAML, SQL, Redis)
├── service/      # Lógica de negócio
├── config/       # Wrappers de configuração
├── factory/      # Criação de objetos complexos
└── Plugin.java   # Bootstrap mínimo
```

---

## Padrão Recomendado por Camada

**Command** — sem lógica, delega ao service:
```java
public final class BalanceCommand implements CommandExecutor {

    private final EconomyService economyService;

    public BalanceCommand(EconomyService economyService) {
        this.economyService = economyService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Apenas jogadores!"));
            return true;
        }
        economyService.showBalance(new PlayerId(player.getUniqueId()));
        return true;
    }
}
```

**Listener** — sem lógica, delega ao service:
```java
public final class JoinListener implements Listener {

    private final WelcomeService welcomeService;

    public JoinListener(WelcomeService welcomeService) {
        this.welcomeService = welcomeService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        welcomeService.welcome(new PlayerId(event.getPlayer().getUniqueId()));
    }
}
```

**Repository** — interface + implementação:
```java
public interface PlayerRepository {
    Optional<PlayerData> findById(PlayerId id);
    void save(PlayerData data);
    void delete(PlayerId id);
}
```

---

## Arquitetura Avançada & SOLID

### Aberto/Fechado (OCP) + Inversão de Dependência (DIP)

Sistemas devem depender de **abstrações (interfaces)**, não de implementações concretas. Novos comportamentos (moedas, formas de pagamento, fontes de dados) são adicionados criando novas classes — **nunca** injetando mais `if/else` no código central. O plugin depende de `PlayerRepository`, sem saber se por trás é YAML, MySQL ou MongoDB.

### ✅ Bom
```java
public interface PaymentMethod {
    boolean hasEnough(PlayerId id, CoinAmount amount);
    void charge(PlayerId id, CoinAmount amount);
}

// Fechado para modificação, aberto para aceitar qualquer moeda via interface
public final class ExpansionService {

    private final RegionRepository repository;

    public ExpansionService(RegionRepository repository) {
        this.repository = repository;
    }

    public void expand(PlayerId id, PaymentMethod payment, CoinAmount cost, Region region) {
        if (!payment.hasEnough(id, cost)) {
            throw new InsufficientFundsException();
        }
        payment.charge(id, cost);
        region.expand(5);
    }
}
```

### Princípio de Hollywood: Arquitetura Baseada em Eventos

*"Don't call us, we'll call you."* Não force um service a conhecer todos os outros sistemas para avisar que algo aconteceu. Dispare um **Custom Event** do Bukkit; quem tiver interesse (quests, scoreboard, chat) que escute isoladamente.

### 🛑 Ruim (acoplamento rígido)
```java
public void expand(Region region) {
    region.expand(5);
    questService.check(region.owner());   // precisa conhecer quests
    scoreboardService.update(region);      // e scoreboard
}
```

### ✅ Bom (acoplamento zero)
```java
public void expand(Region region) {
    region.expand(5);
    Bukkit.getPluginManager().callEvent(new RegionExpandEvent(region));
}
```

---

## Regras Específicas de Produção (PaperMC)

Pontos que mais quebram servidor de verdade. Tratar como obrigatórios.

### 1. Threading: Sync vs Async

A API do Bukkit é **single-threaded** (main thread). IO pesado (banco, HTTP, arquivos) vai para uma task assíncrona; **nunca** toque na API do Bukkit fora da main thread.

### 🛑 Ruim
```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    PlayerData data = database.load(event.getPlayer().getUniqueId()); // bloqueia a main thread
    event.getPlayer().sendMessage("Carregado!");
}
```

### ✅ Bom
```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    UUID playerId = event.getPlayer().getUniqueId();
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        PlayerData data = database.load(playerId);          // IO em async
        Bukkit.getScheduler().runTask(plugin, () ->          // volta à main p/ tocar a API
            applyData(playerId, data));
    });
}
```

### 2. Nunca guarde `Player` / `Entity`

Armazene por `UUID` (ou `PlayerId`). Segurar a referência de um jogador que saiu causa memory leak.

### 🛑 Ruim
```java
private final Map<Player, Integer> kills = new HashMap<>();
```

### ✅ Bom
```java
private final Map<UUID, Integer> kills = new HashMap<>();
// resolva o Player só quando precisar: Bukkit.getPlayer(uuid)
```

### 3. Limpeza no `onDisable`

Cancele tasks, feche conexões e salve dados pendentes ao desligar.

```java
@Override
public void onDisable() {
    Bukkit.getScheduler().cancelTasks(this);
    repository.flush();   // salva pendências
    database.close();     // fecha o pool de conexões
}
```

### 4. Adventure puro (sem `§` / `&`)

Use `Component` e `MiniMessage`. Nunca códigos legados de cor com `§` ou `&`.

### 🛑 Ruim
```java
player.sendMessage("§aVocê recebeu §e100 §acoins!");
```

### ✅ Bom
```java
player.sendMessage(miniMessage.deserialize("<green>Você recebeu <yellow>100</yellow> coins!"));
```

### 5. Sem singleton estático / abuso de `static`

Nada de `Plugin.getInstance()` ou estado global estático. Injete dependências pelo construtor.

### 🛑 Ruim
```java
public final class EconomyService {
    public void pay(UUID id) {
        MyPlugin.getInstance().getRepository().save(...); // acoplamento global
    }
}
```

### ✅ Bom
```java
public final class EconomyService {

    private final PlayerRepository repository;

    public EconomyService(PlayerRepository repository) {
        this.repository = repository;
    }
}
```

### 6. Domínio testável (independente de Bukkit)

Mantenha a lógica de negócio livre de tipos do Bukkit, para testar sem o servidor (unit tests / MockBukkit). O domínio fala em `PlayerId`/`CoinAmount`; a tradução para `Player` fica na borda (command/listener).

---

## Javadoc

Documente a **API pública** em inglês: interfaces, services, value objects, custom events e métodos não triviais. Não documente o óbvio (getters, `onEnable`) nem código privado simples.

Regras:
- Inglês, frase imperativa curta no resumo: *"Charges the player..."*, não *"This method charges..."*.
- `@param`, `@return` e `@throws` em todo método público com parâmetros, retorno ou exceção relevante.
- `{@link}` para referenciar tipos relacionados.
- Documente **o contrato e os efeitos colaterais** (thread-safety, async, eventos disparados), não a implementação.

### Interface / contrato
```java
/**
 * Persists and retrieves player data from the underlying storage.
 *
 * <p>Implementations may be backed by YAML, SQL or any other source and are
 * expected to be thread-safe, as calls may originate from asynchronous tasks.
 */
public interface PlayerRepository {

    /**
     * Finds the stored data for the given player.
     *
     * @param id the unique identifier of the player; never {@code null}
     * @return the player data, or {@link Optional#empty()} if none is stored
     */
    Optional<PlayerData> findById(PlayerId id);

    /**
     * Saves the given player data, overwriting any existing entry.
     *
     * @param data the data to persist; never {@code null}
     */
    void save(PlayerData data);
}
```

### Value object
```java
/**
 * An amount of in-game currency.
 *
 * @param value the amount; must be zero or positive
 */
public record CoinAmount(int value) {

    public CoinAmount {
        if (value < 0) {
            throw new IllegalArgumentException("Coins cannot be negative");
        }
    }
}
```

### Service (efeitos colaterais explícitos)
```java
public final class ExpansionService {

    private final RegionRepository repository;

    public ExpansionService(RegionRepository repository) {
        this.repository = repository;
    }

    /**
     * Charges the player and expands the given region.
     *
     * <p>Must be called from the main server thread, as it interacts with the
     * Bukkit API. Fires a {@code RegionExpandEvent} on success.
     *
     * @param id      the paying player
     * @param payment the payment method used to charge the cost
     * @param cost    the price of the expansion
     * @param region  the region to expand
     * @throws InsufficientFundsException if the player cannot afford the cost
     */
    public void expand(PlayerId id, PaymentMethod payment, CoinAmount cost, Region region) {
        if (!payment.hasEnough(id, cost)) {
            throw new InsufficientFundsException();
        }
        payment.charge(id, cost);
        region.expand(5);
    }
}
```

---

## Checklist de Revisão

Antes de gerar ou aprovar qualquer código, verifique:

- [ ] Métodos com no máximo 1 nível de indentação?
- [ ] Nenhum `else` desnecessário? (early return ou polimorfismo)
- [ ] Expressões complexas extraídas para variáveis explicativas?
- [ ] Cada método faz uma única coisa?
- [ ] Primitivos/Strings de domínio em Value Objects?
- [ ] Coleções encapsuladas em classes próprias?
- [ ] `Optional` em vez de `null`?
- [ ] Sem raw types; generics usados em coleções e abstrações?
- [ ] Wildcards corretos (PECS: `? extends` produz, `? super` consome)?
- [ ] Sem casts inseguros espalhados (`@SuppressWarnings` isolado e justificado)?
- [ ] Records para DTOs e dados imutáveis?
- [ ] Exceções específicas e informativas (nenhum `catch` vazio ou genérico)?
- [ ] Retornos de coleções internas blindados (cópias imutáveis/read-only)?
- [ ] NBT keys e caminhos de config fortemente tipados (sem strings mágicas)?
- [ ] Sistemas desacoplados por interfaces e injeção de dependência (DIP)?
- [ ] Comportamentos novos adicionados por extensão, não por `if/else` central (OCP)?
- [ ] Comunicação entre sistemas via Custom Events (Princípio de Hollywood)?
- [ ] IO/banco em task assíncrona, sem tocar a API do Bukkit fora da main thread?
- [ ] Jogadores guardados por `UUID`/`PlayerId`, nunca como `Player`/`Entity`?
- [ ] `onDisable` cancela tasks, salva pendências e fecha conexões?
- [ ] Mensagens em `Component`/MiniMessage, sem `§`/`&` legado?
- [ ] Dependências injetadas pelo construtor, sem singleton estático/`static` global?
- [ ] Domínio testável, independente de tipos do Bukkit?
- [ ] API pública documentada com Javadoc em inglês (contrato + efeitos colaterais)?
- [ ] Encadeamento de chamadas minimizado (exceto fluent APIs)?
- [ ] Nomes completos e descritivos, sem abreviações?
- [ ] Classes ≤ 150 linhas, métodos ≤ 15 linhas?
- [ ] Classes com no máximo 2–3 variáveis de instância?
- [ ] Lógica dentro dos objetos (Tell, Don't Ask)?
- [ ] Bootstrap mínimo, sem lógica em Command/Listener?
- [ ] Comentários redundantes removidos? (código autoexplicativo)
- [ ] Código formatado com `google-java-format` (`spotlessCheck` verde)?

---

## Revisão Final (Antes de Entregar)

Nenhum código é considerado pronto sem passar por **todas** estas etapas. Revise o conjunto inteiro, não só o trecho alterado (Princípio do Escoteiro).

1. **Reler o diff completo** com olhos críticos — cada arquivo tocado contra o Checklist de Revisão acima, item por item.
2. **Compilar e buildar verde** — `./gradlew build` sem erros e sem warnings novos (inclui `spotlessCheck`).
3. **Rodar os testes** — suíte de unit tests passando; lógica de domínio coberta.
4. **Conferir imports e mortos** — sem imports não usados, código morto, `System.out.println` ou TODOs esquecidos.
5. **Validar Javadoc** — API pública documentada em inglês, sem `@param`/`@return` faltando ou desatualizado.
6. **Verificar threading** — todo IO em async, nada da API do Bukkit fora da main thread.
7. **Verificar limpeza** — recursos abertos no enable são fechados no `onDisable`.
8. **Testar em servidor real** — plugin carrega sem stacktrace no console; comandos e listeners funcionam in-game.

> Só entregue quando todos os 8 passos estiverem ✅. Na dúvida em algum item, volte e corrija antes — não depois.