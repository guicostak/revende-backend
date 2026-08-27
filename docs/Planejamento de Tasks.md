---
tags: [plano, backlog]
---

# Planejamento de Tasks

> Cada task tem uma issue em https://github.com/guicostak/revende-backend/issues — T-01 a T-20 são as issues #1 a #20, D-01 a D-06 são #21 a #26.

Board: [[Kanban]] · Regras: [[Índice de Regras]] · Contrato: [[CLAUDE]]

Cada task tem **critério de aceite verificável**. Task sem teste não fecha — [[CLAUDE]] §3.

## Princípio de ordenação

1. **Rede de segurança antes de mudança estrutural.** Sem teste, refatorar é apostar.
2. **Bug que devolve 500 antes de feature.** Erro de servidor no caminho mais comum
   (login errado) é o que queima confiança primeiro.
3. **Furo de domínio antes de performance.** Venda desfeita é pior que query lenta.
4. **Migração hexagonal por último dentro de cada onda** — ela reescreve o que os testes protegem.

---

## Sprint 1 — rede de segurança

### T-01 · ArchUnit + testes de caracterização
Antes de mover qualquer coisa, travar o comportamento atual.
**Aceite:** teste ArchUnit falha se `domain` importar Spring/JPA ou `application` importar `adapter`;
testes de API cobrindo os 11 casos de uso no comportamento de hoje (inclusive os errados,
marcados com `// characterization: comportamento atual, corrigido em T-0X`).
**Depende de:** nada. **Bloqueia:** T-14 em diante.

### T-02 · Login com senha errada devolve 401
`AuthenticationException` não tem handler → o erro mais comum da app devolve **500**.
**Aceite:** senha errada → 401; e-mail inexistente → 401 **com a mesma mensagem**
(não vazar existência de conta); teste para os dois. Ver [[UC-02 Autenticar]].

### T-03 · Falta de posse devolve 403
`ensureOwner` lança `IllegalArgumentException` → 400.
**Aceite:** `NotListingOwnerException` de domínio + handler → **403**; anúncio não muda;
testes em `PATCH /sold` e `DELETE`. Ver [[RN-006 Apenas o dono altera o anúncio]].

### T-04 · Transição de status inválida devolve 409
Hoje dá para marcar como vendido um anúncio **cancelado**, e cancelar um **vendido** —
apagando o registro da venda. É o furo de domínio mais grave.
**Aceite:** a transição vira método do agregado (`markSoldBy` / `cancelBy`); estado terminal
recusa com **409** e preserva o status; matriz de transição coberta por teste.
Ver [[RN-007 Ciclo de vida do anúncio]].

### T-05 · `/api/listings/me` sem token devolve 401
A rota casa com o padrão público `GET /api/listings/**` → `Authentication` nulo → NPE → 500.
**Aceite:** sem token → 401; libera explicitamente só `GET /api/listings` e `/api/listings/{id}`.
Ver [[UC-08 Listar meus anúncios]].

---

## Backlog — segurança e infraestrutura

**T-06** Boot falha fora do perfil `dev` se `REVENDE_JWT_SECRET` for o default. *Aceite:* teste de contexto com perfil `prod` e sem env var → falha na subida, com mensagem clara.
**T-07** `JwtAuthFilter` distingue token inválido (log em `debug`) de falha inesperada (log em `warn` + stack). *Aceite:* nenhum `catch (Exception ignored)` no código.
**T-08** Flyway com `V1__baseline.sql` gerado do schema atual; `ddl-auto: validate`. *Aceite:* app sobe com banco vazio e com banco existente.
**T-09** `ErrorResponse` record (`timestamp`, `status`, `error`, `message`, `fields`). *Aceite:* todos os handlers devolvem o mesmo formato; teste do contrato.

## Backlog — API e performance

**T-10** `FetchType.LAZY` + consulta com `JOIN FETCH` na vitrine. *Aceite:* número de queries **constante** com 1 e com 50 anúncios (contador de statements no teste).
**T-11** `Pageable` + `Page<T>` em eventos e anúncios, com ordenação explícita. *Aceite:* `?page=&size=&sort=` funcionam; default documentado no README.
**T-12** `@Transactional` nos use cases de escrita, `readOnly = true` nos de leitura. *Aceite:* teste de rollback em falha no meio da publicação.
**T-13** `ticketType` fora do enum → 400 listando os valores aceitos. *Aceite:* teste com valor inválido.

## Backlog — migração hexagonal

Segue os passos de [[Mapa de Migração]], um por task: **T-14** domínio puro ·
**T-15** entidade JPA + mapper + adapter · **T-16** ports de saída · **T-17** services por
caso de uso + ports de entrada · **T-18** controller nas interfaces ·
**T-19** [[Contexto Catalog]] · **T-20** [[Contexto Identity]].

Regra da travessia: **nunca duas estruturas para o mesmo contexto ao mesmo tempo.**

---

## Bloqueadas por decisão

Estas **não entram em sprint** enquanto não houver resposta — implementar sem decisão é
escolher no lugar do negócio. Ver [[Perguntas em Aberto]] e [[Índice de Decisões]].

| # | Decisão | Trava |
|---|---|---|
| D-01 | Quem cadastra evento | papel/role, autoria de evento, curadoria |
| D-02 | Plataforma intermedia transação? | agregado `Order`, `RESERVADO`, pagamento, taxa |
| D-03 | Anúncio não-ativo por ID | [[UC-07 Detalhar anúncio]] |
| D-04 | `city` + `name` juntos | busca combinada |
| D-05 | Teto/sinalização de ágio | precificação, destaque |
| D-06 | Contato comprador↔vendedor | o produto não fecha o ciclo sem isso |

> [!warning] D-02 e D-06 são existenciais
> Sem resposta, o Revende é uma vitrine onde ninguém consegue comprar. Toda task de
> produto acima é polimento em cima dessa lacuna.
