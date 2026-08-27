## O que muda

<!-- Uma frase. O porquê importa mais que o o quê. -->

## Regra de negócio afetada

<!-- Link para a nota em docs/Regras ou docs/Casos de Uso. Se nenhuma, escreva "nenhuma". -->

## Checklist

Contrato completo em [CLAUDE.md](../CLAUDE.md) §3.

- [ ] `mvn -B verify` passa localmente
- [ ] `mvn spotless:apply` rodado (a CI reprova formatação divergente)
- [ ] Nenhum import de framework entrou em `domain/`
- [ ] Regra de negócio nova está no domínio, com teste unitário
- [ ] Caminho de erro tratado, com o status HTTP correto
- [ ] Nenhum segredo em código, log ou configuração versionada
- [ ] Listagem nova é paginada e sem N+1
- [ ] A nota da regra afetada foi atualizada **neste mesmo PR**
