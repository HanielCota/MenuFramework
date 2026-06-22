# Revisão técnica consolidada

## Problemas encontrados e corrigidos

### Lock global envolvendo `openInventory()`

A abertura era executada sob o mesmo monitor usado pelo shutdown. Isso
serializava jogadores diferentes e poderia manter um lock global durante eventos
de outros plugins. A publicação da sessão agora é curta e protegida; a chamada
Bukkit ocorre fora do monitor.

### Sessões desconectadas durante shutdown

Durante substituição, a sessão anterior deixa temporariamente o mapa de sessões
atuais. Foi adicionado um conjunto separado de sessões vivas para garantir que o
shutdown também cancele sessões em transição.

### Interferência entre múltiplas instâncias

O holder possuía somente `sessionId`. Agora contém também `runtimeId`, e cada
listener processa apenas inventários da própria instância do framework.

### Estado, frame e revisão publicados separadamente

Leituras concorrentes podiam observar combinações inconsistentes. Os três
valores agora formam um único snapshot volátil.

### Aplicação parcial de frame

Uma exceção em `Inventory#setItem` podia deixar metade do novo frame aplicada.
O applier registra o conteúdo anterior e executa rollback reverso. Também valida
sessão e view antes da renderização, antes do diff e antes do commit.

### Diff baseado apenas no frame anterior

Uma alteração inesperada feita por outro plugin não era reparada quando o frame
lógico permanecia igual. O diff agora compara com o conteúdo real do inventário.

### Substituição prematura de tasks

A geração é reservada antes da criação da substituta, mas a task anterior só é
cancelada quando a nova operação é publicada. Falhas anteriores à publicação não
cancelam uma task saudável.

### Respostas assíncronas antigas

Conclusões agora precisam corresponder à sessão, chave, geração e handle ativos,
além de a view correta continuar aberta.

### Falha ao renderizar sucesso assíncrono

O menu poderia permanecer indefinidamente em loading. O runtime tenta a
transição de falha para produzir um estado recuperável.

### Histórico restaurado com geração antiga

A geração pertence à nova sessão, não ao snapshot de estado salvo. O estado
assíncrono restaurado pode iniciar novamente na geração um.

### Acoplamento do enum público ao Bukkit

O mapeamento de `InventoryCloseEvent.Reason` foi movido para uma classe interna.
A API pública não depende mais dos valores exatos do enum da plataforma.

### Erros fatais tratados como falhas recuperáveis

`Error` não é convertido em falha comum de menu. Ele é propagado; exceções não
fatais de código consumidor são encaminhadas ao handler central.

## Limites intencionais da versão 1.0

- Somente chest inventories de uma a seis linhas.
- Título e layout são estruturais durante uma sessão.
- O inventário superior não é editável.
- Estado e entradas de histórico não recebem deep copy; devem ser imutáveis.
- Cancelamento de futures e scheduled tasks é best effort.
- O booleano das operações públicas indica aceitação de agendamento, não sucesso
  final da operação.
- O shutdown não chama callbacks de menu.
- Várias tasks periódicas solicitadas no mesmo callback são iniciadas por chave;
  não existe rollback global se uma tarefa posterior for rejeitada.
- Compatibilidade Folia depende também de o código do plugin consumidor respeitar
  as regras de região.

## Validação ainda obrigatória no plugin consumidor

- Executar a suíte com o JDK e Paper API do servidor alvo.
- Testar abertura, fechamento, quit, morte e troca de mundo em servidor real.
- Testar paginação assíncrona com respostas fora de ordem.
- Testar sob Folia com teleporte entre regiões.
- Fazer teste de carga e observar retenção de sessões/futures.
