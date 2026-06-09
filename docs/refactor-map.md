# Refactor Map — Fase 0 (Inventário, ANTES de tocar em nada)

> Objetivo deste documento: mapear o framework para o refactor de **simplificação
> (só remove)**. Nada foi alterado nesta fase. As colunas "DEPOIS" e a contagem
> final serão preenchidas ao fim de cada fase.
>
> **Rede de segurança:** `125` arquivos de teste · `605` métodos de teste.
> Build verde + esses testes passando é o gate de cada fase.

## Contagem ANTES (arquivos `.java` em `main/`)

| Módulo        | Classes/Records/Interfaces (arquivos main) |
|---------------|---------------------------------------------|
| menu-core     | 79 |
| menu-paper    | 70 |
| menu-folia    | 3  |
| example-plugin| 6  |
| **TOTAL**     | **158** |

Dos 158: **21 interfaces**, 2 enums, ~32 records, resto classes.

Contagem DEPOIS: _(a preencher)_

---

## 1. Inventário por package

Classes por package (main):

```
core  placeholder ........ 1   ⚠ package de 1 classe
core  click .............. 2
core  discovery .......... 2
core  scheduler .......... 3   (interfaces puras — seam Paper/Folia)
core  state .............. 3
core  action ............. 4
core  compiler ........... 4
core  config ............. 4
core  merge .............. 3
core  item ............... 5
core  domain ............. 7
core  template ........... 7
core  annotation ......... 8   (anotações puras)
core  compiler/model ..... 7
core  compiler/reader .... 6
core  compiler/binding ... 13  (maior package)
paper  (raiz) ............ 7   (bootstrap/facade)
paper api ................ 8
paper view ............... 8
paper discovery .......... 6
paper reactive ........... 6
paper registry ........... 5
paper render ............. 5
paper render/cache ....... 3
paper render/model ....... 3
paper listener ........... 3
paper holder ............. 3
paper argument ........... 3
paper placeholder ........ 3
paper scheduler .......... 3
paper anvil .............. 2
paper hook ............... 2
folia .................... 3
```

Nenhum package ultrapassa o limite de ~10 (binding=13 é o único acima, e é coeso:
todo o boot-time binding de @Button/@Tick/@Reactive/@Viewer + guards).

---

## 2. Interfaces — implementações reais e uso em teste (FASE 1)

Critério: 1 impl real **e** sem mock/teste-double dependente → cortar. 2+ impls reais,
ou substituição legítima em teste, ou ponto de extensão público documentado → mantém.

| Interface | Pkg | Impls reais | Teste depende de mock/double? | Veredito | Justificativa |
|-----------|-----|-------------|-------------------------------|----------|---------------|
| `MenuScheduler` | core/scheduler | 2 (Paper, Folia) | — | **MANTÉM** | seam real Paper/Folia |
| `PlayerScheduler` | core/scheduler | 2 (Paper, Folia) | — | **MANTÉM** | seam real Paper/Folia |
| `ScheduledTask` | core/scheduler | 2 (Paper, Folia) | — | **MANTÉM** | seam real Paper/Folia |
| `ClickArgumentResolver` | core/action | 3 (ClickContextResolver, MenuClick…, Player…) | sim (lambdas) | **MANTÉM** | SPI extensível, 3 impls |
| `IconFactory<V>` | core/template | 2 (ItemFactory, ResolvedIconFactory) | sim (lambda) | **MANTÉM** | 2 impls (com/sem placeholder) |
| `MenuOpener` | paper/api | 2 (DeferredMenuOpener, MenuRegistry) | sim (doubles) | **MANTÉM** | 2 impls; quebra ciclo de init |
| `ClickableHolder` | paper/holder | 2 (MenuHolder, ReactivePagedView) | sim | **MANTÉM** | 2 impls (estático + reativo) |
| `OpenMenu` | paper/holder | 2 (MenuHolder, ReactivePagedView) | sim | **MANTÉM** | 2 impls |
| `PaperMenu` | paper/view | 2–3 (StaticPaperMenu, ReactivePagedMenu) | sim | **MANTÉM** | 2 impls (estático + paginado) |
| `MenuAction` | core/action | SAM (lambdas em todo lugar) | sim | **MANTÉM** | contrato funcional central |
| `ButtonArguments` | core/action | SAM (lambdas em ClickArguments) | sim | **MANTÉM** | contrato funcional (forma do arg) |
| `ClickContext` | core/click | 1 real (PaperClickContext) | **sim** (`new ClickContext(){}` em vários testes) | **MANTÉM** | seam core↔plataforma + substituição em teste |
| `StateListener` | core/state | 1 (ReactivePagedView) | parcial | **MANTÉM** | DIP real: `State` (core) não pode depender da view (paper) |
| `PlaceholderResolver` | core/placeholder | 1 (PapiPlaceholders) | sim (lambda + factory) | **MANTÉM** | SAM público, seam para PAPI (soft-dep) |
| `InventoryFactory` | paper/render | 1 (BukkitInventoryFactory) | **sim** (`mock(…)` + RecordingFactory) | **MANTÉM** | substituição legítima em teste |
| `MenuErrorHandler` | paper/api | 0 no main (default + user) | sim | **MANTÉM** | extensão pública (`builder.onActionError`), documentada |
| `MenuInstanceFactory` | paper/discovery | 1 (MenuInstantiator) | — | **MANTÉM** | extensão pública (`builder.instantiator`, p/ DI), documentada em AGENTS.md/llms.txt |
| **`MenuDiscovery`** | core/discovery | 1 (ClassGraphMenuDiscovery) | não (testes usam a concreta) | **🔪 CORTAR** | sem 2ª impl, sem hook de troca, sem mock — o prompt já sinaliza |
| **`ReactiveView`** | paper/reactive | 1 (ReactivePagedView) | não | **🔪 CORTAR (verificar)** | só usado em `instanceof` por MenuListener/Lifecycle; vira `instanceof ReactivePagedView` |
| **`AnvilPromptOpener`** | paper/api | 1 (AnvilPrompts) | não | **⚠ REVISAR** | usado p/ inverter ciclo api↔anvil em MenuClick; cortar só se o ciclo for tolerável |
| **`CompiledMenuVisitor<V,R>`** | core/compiler/model | 1 (MenuFactory) | sim (LabelVisitor) | **⚠ REVISAR** | Visitor sobre `CompiledMenu` *sealed* (2 variantes); `switch` de pattern-matching elimina a interface, mas é reestruturação (não delete puro) e o teste depende dela |

**Resumo Fase 1 — VEREDITO FINAL APÓS VERIFICAÇÃO: 0 cortes.** Os 2 candidatos firmes
caíram na verificação (a regra do prompt manda manter onde há substituição em teste):

- **`MenuDiscovery` → MANTÉM.** `MenuScannerTest`/`MenuScannerEdgeCasesTest` usam um
  test-double via `discoveryOf(...)` → `packages -> menus` (lambda da interface) para
  alimentar `MenuScanner` com classes `@Menu` controladas, isolando-o do ClassGraph real.
  Substituição em teste legítima → exceção explícita do prompt.
- **`ReactiveView` → MANTÉM.** `MenuFrameworkBuilderTest:105` faz `mock(ReactiveView.class)`
  para verificar teardown no shutdown sem montar uma view real. Mock-dependente +
  interface-papel usada em `instanceof` por `MenuListener` e `MenuLifecycle`.
- `AnvilPromptOpener`, `CompiledMenuVisitor` → MANTÉM (decisão conservadora confirmada).
- `MenuInstanceFactory`, `MenuErrorHandler`, `PlaceholderResolver`, `ClickArgumentResolver`
  → MANTÉM (API pública documentada em AGENTS.md/llms.txt; cortar = mudança de API).

**Achado:** a camada de interfaces NÃO está over-engineered — cada abstração se paga
via testabilidade (substituição/mock) ou seam real Paper/Folia. Não fabriquei cortes
para atingir uma "redução" esperada.

---

## 3. Classes suspeitas de SRP ("faz X E Y") — FASE 3

Marcadas pelos leitores. Nem toda marca vira split: caching costuma ser parte coesa
de "ler/renderizar". Avaliar caso a caso; **só separar se gerar mais clareza que fiação**.

Decisão: **atacar todos** (escolha do usuário, aceitando classes concretas novas).
Resultado: **4 splits feitos, 4 rejeitados** após leitura próxima. Splits usam classes
CONCRETAS (não interfaces), API pública estável (delegação), comportamento idêntico.

| Classe | Linhas (antes→depois) | Veredito | O que foi feito / por que não |
|--------|----------------------:|----------|-------------------------------|
| `MenuRegistry` | 315 → **224** | ✅ **SPLIT** | Extraído `MenuReloader` (~147): toda a máquina de reload sync/async/report + records `PreparedReload(s)`. Registry mantém register/open + delega reloads. |
| `PagedReader` | 317 → **95** | ✅ **SPLIT** | Extraído `PagedStructureReader` (reflexão) + `PagedMetadata` (record, agora top-level). PagedReader vira fachada de cache + API. |
| `HookDefinitions` | 143 → **52** | ✅ **SPLIT** | Extraído `HookReader` (reflexão) + `Handler` (um hook). HookDefinitions = cache + bind. |
| `MenuLoader` | 172 → **133** | ✅ **SPLIT** | Extraído `MenuConfigValidator` (regras estruturais, static). MenuLoader = IO/parse/cache. |
| `ReactivePagedView` | 142 | ❌ **REJEITADO** | Já é um coordenador fino: delega render→`PageRenderer`, página→`PageCursor`, binding/ticks/teardown→`ReactiveLifecycle`. 3 campos. As 4 interfaces são facetas de "a view aberta", não responsabilidades separáveis. Split duplicaria refs e pioraria. |
| `AnvilPrompt` | 176 | ❌ **REJEITADO** | Já decomposto em records `Display` + `Resolution`; 2 campos; 176 linhas são quase só Javadoc de API pública. Expor os records vazaria internals sem ganho. |
| `ItemFactory` | 139 | ❌ **REJEITADO** | Pipeline coeso de "construir um ItemStack"; caches são detalhe de performance justificado no Javadoc. |
| `PageRenderer` | 192 | ❌ **REJEITADO** | "Renderizar uma página" — uma responsabilidade; métodos `place*`/`fill*` nomeiam etapas de domínio. |

---

## 4. Métodos privados — candidatos a inline (FASE 2)

Critério: privado chamado **1x** que **não** nomeia conceito de domínio e fragmenta
leitura. **Manter** os chamados 2+x ou que nomeiam conceito (`placeNavigation`,
`sweepExpired`, `resolveSlotFromMask`, etc.).

**VEREDITO FINAL APÓS LEITURA: 1 inline aplicado, resto rejeitado com justificativa.**

- ✅ **APLICADO — `DiffWriter.changed` + `DiffWriter.apply`:** dois one-liners triviais
  (`!Objects.equals(...)` e `inventory.setItem(...)`) que fragmentavam um pipeline de 3
  linhas. Inlinados nos lambdas do `write()`; lê melhor de cima a baixo. `snapshot` (faz
  array-copy com clamp, nomeia conceito) ficou.
- ❌ **REJEITADO — `Instantiator.invoke`, `UnboundTick.invoke`, `ButtonActions.invoke`:**
  nomeiam o conceito "invocar o handle traduzindo falha de `MethodHandle`"; inliná-los
  jogaria try/catch para dentro de lambdas/delegação-de-construtor (`this(() -> ...)`),
  piorando a leitura. Idiom consistente nas 3 classes.
- ❌ **REJEITADO — `MaskLayout.validateShape/validateWidth/validateRole`:** formam uma
  gramática de validação em descida recursiva (grade → linha → célula), cada nível
  nomeia um conceito e usa method-reference. Inline criaria lambdas aninhados — pior.
- ❌ **REJEITADO — `Flusher.run`:** alvo de `this::run` passado ao scheduler (callback
  agendado), não um fragmento; nomeia o conceito do flush.
- `SchedulerFactory.detectFolia` / `AnvilPrompts.nameTag` / `State.notifyListener` /
  `ButtonConfig.traits` → mantidos (nomeiam conceito). `detectFolia` duplicado: ver §3.

**Manter (nomeiam conceito, mesmo 1x):** `State.notifyListener`, `ButtonConfig.traits`,
`ContentProvider.requireItems`, `Cooldown.sweepExpired`, e a maioria dos `place*` /
`fill*` de `PageRenderer` e dos `validate*` / `ensure*` de `PagedMerger`/`StaticMerger`
(nomeiam regras de domínio e o método-pai ficaria ilegível inline).

**Nota (duplicação):** `detectFolia` existe em `SchedulerFactory` **e** `MenuLifecycle`.
Não é inline — é possível duplicação a consolidar. Anotar p/ Fase 3 (sem criar abstração nova).

---

## 5. Value Objects / wrappers (FASE 4)

Diagnóstico honesto: **o codebase quase não tem wrapper nu.** A maioria valida invariante
ou tem comportamento — ficam.

| Tipo | Valida? | Comportamento? | Veredito |
|------|---------|----------------|----------|
| `MenuId` | sim (pattern + tamanho) | — | **MANTÉM** |
| `Slot` | sim (range) | factory | **MANTÉM** |
| `PageNumber` | sim (≥0) | next/previous/first | **MANTÉM** |
| `ButtonId` | sim (não-vazio) | — | **MANTÉM** |
| `PlayerId` | null-check | — | **MANTÉM** (identidade de domínio pública; evita primitive obsession) |
| `PagedDecor` (record 3 campos) | não | não | **⚠ REVISAR** — não é wrapper de 1 valor, é agregado de 3 visuais; avaliar fundir, baixa prioridade |

Não há `record Wrapper(X value)` sem nada a cortar. Fase 4 será leve.

---

## 6. Packages / classes soltas (FASE 5)

- `core/placeholder` tem **1 classe** (`PlaceholderResolver`, interface). É fronteira
  pública (seam PAPI). Avaliar fundir em `core/...` vizinho **ou** manter como fronteira.
  Baixa prioridade.
- Nenhuma classe "solta" sem package. Estrutura geral é coerente por responsabilidade.
- `compiler/binding` (13) é o maior, mas coeso.

---

## 7. Resultado por fase (executado)

1. **Fase 1 — interfaces:** **0 cortes.** Ambos os candidatos firmes têm seam de teste
   real (verificado). A camada de interfaces não estava over-engineered.
2. **Fase 2 — inline:** **1 aplicado** (`DiffWriter.changed`+`apply`). Demais rejeitados
   (nomeiam conceito; inline pioraria).
3. **Fase 3 — SRP:** **4 splits** (MenuRegistry, PagedReader, HookDefinitions, MenuLoader),
   **4 rejeições** justificadas (já coesos / já decompostos). +6 classes concretas.
4. **Fase 4 — wrappers:** pulado (decisão do usuário; não havia wrapper nu além de
   `PagedDecor`, que é agregado de 3 campos, não wrapper de 1 valor).
5. **Fase 5 — packages:** pulado (decisão do usuário; `core/placeholder` é fronteira
   pública PAPI).

Formatação: `spotlessApply` aplicado (formatter é fonte da verdade per CLAUDE.md);
corrigiu de quebra 3 arquivos de `example-plugin` com drift pré-existente.
Build + 605 testes verdes ao fim de cada fase. Comportamento runtime idêntico.

---

## 8. Contagem ANTES → DEPOIS

| | Arquivos main | Interfaces |
|--|-------------:|-----------:|
| ANTES | 158 | 21 |
| DEPOIS | 164 | 21 |

> **Nota honesta sobre a contagem:** o número de arquivos **subiu** (+6), não desceu.
> Isso é a consequência direta da decisão "atacar todo SRP de verdade": separar
> responsabilidades = mais classes, menores. As classes-alvo encolheram muito
> (PagedReader 317→95, HookDefinitions 143→52, MenuRegistry 315→224, MenuLoader
> 172→133). A "redução" esperada pelo prompt não se materializou porque o framework
> **já estava enxuto na camada que o prompt presumia inchada** (interfaces órfãs,
> wrappers nus, métodos privados fragmentados): quase nada disso existia. O ganho real
> foi coesão (SRP), não remoção.
</content>
