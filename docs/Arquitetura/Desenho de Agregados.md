---
tags: [dominio, referencia, ddd]
---

# Desenho de Agregados

As quatro regras de Vaughn Vernon, e o que elas significam para o [[Contexto Marketplace]].

## As regras

### 1. Proteja invariantes verdadeiras dentro da fronteira de consistência

**Uma transação altera um agregado.** Se duas coisas precisam mudar juntas ou o dado fica
inconsistente, elas pertencem ao mesmo agregado. Se não precisam, não pertencem.

### 2. Projete agregados pequenos

O menor agregado possível é **uma entidade só**, com o mínimo de atributos necessários para
existir num estado válido. Agregado grande limita desempenho e escala mesmo quando toda
transação passa — porque carrega dado que ninguém pediu e vira ponto de contenção.

> Vernon recomenda **começar por esta regra**: cada entidade vira raiz de agregado, e só
> depois você junta o que a invariante obrigar.

### 3. Referencie outros agregados por identidade

Nunca por referência de objeto. `TicketListing` guarda `SellerId`, não `User`.

Três ganhos diretos: o agregado fica menor automaticamente, nada é carregado avidamente por
acidente, e a instância ocupa menos memória e sobe mais rápido.

**A v1 do Revende violava isso**: `TicketListing` tinha `@ManyToOne` para `Event` e `User`,
ambos `EAGER` — que é exatamente o N+1 que aparecia na vitrine.

### 4. Use consistência eventual fora da fronteira

O que precisa mudar em outro agregado muda **depois**, por evento — não na mesma transação.

## Aplicando ao Revende

| Candidato | Agregado próprio? | Por quê |
|---|---|---|
| `TicketListing` | ✅ raiz | Tem invariante real: posse e transição de estado |
| `User` | ✅ raiz | Identidade própria, ciclo de vida independente |
| `Event` | ✅ raiz | Existe sem anúncio; anúncio referencia por `EventId` |

Nenhum contém o outro. As três regras acima já respondem: não há invariante que exija
mudar anúncio e evento na mesma transação.

**A invariante que a v1 não tinha** e que justifica o agregado: só o dono altera
([[RN-006 Apenas o dono altera o anúncio]]) e estado terminal não volta
([[RN-007 Ciclo de vida do anúncio]]). Isso mora dentro de `TicketListing`, não no service.

## Fontes

- [Effective Aggregate Design — Vaughn Vernon](https://www.dddcommunity.org/library/vernon_2011/)
- [Rule: Design Small Aggregates — InformIT](https://www.informit.com/articles/article.aspx?p=2020371&seqNum=3)
- [Rule: Reference Other Aggregates by Identity — InformIT](https://www.informit.com/articles/article.aspx?p=2020371&seqNum=4)
- [Aggregate Design Rules — ArchiLab](https://www.archi-lab.io/infopages/ddd/aggregate-design-rules-vernon.html)
