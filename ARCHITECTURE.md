# Arquitetura

## Modelo

```text
Menu<S>                  definição reutilizável
MenuSession<S>           uma abertura para um jogador
MenuFrame<S>             snapshot imutável da renderização
MenuCanvas<S>            builder de frame, válido por uma renderização
MenuManager              fachada pública
MenuLifecycleCoordinator abertura, fechamento, navegação e callbacks
MenuInteractionDispatcher execução transacional de botões
MenuFrameApplier         diff visual, rollback e publicação de estado
MenuAsyncTaskRuntime     operações assíncronas por chave e geração
MenuPeriodicTaskRuntime  tarefas periódicas por sessão
MenuSessionRegistry      sessão atual e sessões vivas
MenuHistoryRegistry      histórico limitado por jogador
MenuScheduler            fronteira Paper/Folia
```

## Invariantes

- Cada jogador possui no máximo uma sessão atual por instância do framework.
- Cada abertura recebe `sessionId` único.
- Cada framework recebe `runtimeId` único; listeners de instâncias diferentes
  não interferem entre si.
- A view é identificada pelo holder do inventário superior, nunca pelo título.
- O despacho usa `session + rawSlot`, nunca material, nome, lore ou PDC.
- Renderização produz um frame completo antes de aplicar alterações.
- Estado, frame e revisão são publicados em um único snapshot volátil.
- O diff compara com o conteúdo real do inventário e repara mutações externas.
- Aplicação parcial de frame sofre rollback em ordem reversa.
- Uma task ativa é identificada por `session + key + generation + handle`.
- Fechamento e descarte são idempotentes.
- O histórico só é confirmado depois que a nova view é aberta e validada.

## Modelo de threads

Abertura, renderização, clique, navegação e tasks periódicas executam no
`EntityScheduler` do jogador. Operações demoradas usam o `AsyncScheduler`.
Conclusões assíncronas retornam ao scheduler da entidade antes de ler a view,
alterar estado ou renderizar.

O registry compartilhado é concorrente, mas o estado mutável normal de cada
sessão continua confinado ao scheduler da entidade. Locks internos existem
somente para ciclo de vida curto e registry de tasks; chamadas Bukkit e código
do consumidor nunca executam sob o lock global de lifecycle.

## Renderização transacional

```text
Menu.render()
    -> DefaultMenuCanvas
    -> MenuFrame completo
    -> validação de sessão/view
    -> cálculo do diff contra o inventário real
    -> aplicação dos slots alterados
    -> nova validação
    -> commit atômico de state/frame/revision
```

Se uma mutação de slot ou o commit falhar, os slots já alterados são restaurados.
Se a sessão deixar de ser atual durante a aplicação, o diff é desfeito e o frame
não é publicado.

## Navegação

Aberturas externas criam uma raiz e limpam o histórico. `interaction.open()`
registra um snapshot do menu anterior. `back()` restaura o menu e a referência de
estado capturada. A transição da pilha é transacional: abertura cancelada ou
inválida preserva o histórico anterior.

## Shutdown

`shutdown()` impede novo trabalho, limpa o histórico, remove sessões dos
registries e cancela recursos pertencentes às sessões. Não manipula views nem
executa `onClose`, pois o desligamento do plugin não garante contexto de região
seguro no Folia.
