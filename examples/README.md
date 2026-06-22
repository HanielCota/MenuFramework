# Exemplos

Os arquivos desta pasta não fazem parte do source set principal da biblioteca, mas são compilados pelo source set `examples` durante `check`. Copie os exemplos desejados para o plugin. Todos já utilizam o namespace `com.hanielfialho.menuframework.example`.

Os menus demonstram o caminho recomendado de DX: slots nomeados, componentes reutilizáveis, tema padrão e feedback sonoro configurado uma vez no `ExamplePlugin`.

- `CounterMenu`: estado imutável, botão e navegação.
- `SettingsMenu`: toggles simples e `PLAYER_INVENTORY_ALLOWED`.
- `ConfirmationMenu`: confirmação/cancelamento antes de executar uma ação.
- `CountdownMenu`: task periódica que atualiza a sessão até finalizar.
- `SynchronousProductMenu`: paginação sobre snapshot em memória.
- `AsyncProductMenu`: loading, ready, error e retry.
- `ExamplePlugin`: inicialização e desligamento corretos.

O exemplo assíncrono usa `CompletableFuture.completedFuture` porque o
`PageSource` já é invocado pelo scheduler assíncrono do runtime. A consulta real
pode ser bloqueante nesse ponto, mas deve retornar apenas objetos de domínio
imutáveis e nunca acessar APIs Bukkit dependentes de região.
