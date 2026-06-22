# Changelog

## 1.0.0

- Namespace definitivo `com.hanielfialho.menuframework`.
- API pública organizada por domínio e runtime distribuído em subpackages `internal`.
- Spotless configurado com Google Java Format e validação integrada ao `check`.
- Sessões isoladas por jogador e por instância de runtime.
- Frames completos, diff visual e rollback transacional.
- Navegação para frente e histórico com `back()`.
- Paginação síncrona e assíncrona.
- Operações assíncronas por chave e geração.
- Tarefas periódicas pertencentes à sessão.
- Políticas `READ_ONLY` e `PLAYER_INVENTORY_ALLOWED`.
- Tratamento centralizado de erros e contexto de observabilidade.
- Scheduler compatível com Paper e arquitetura preparada para Folia.
- Javadocs e suíte de testes de caracterização/integracão.
