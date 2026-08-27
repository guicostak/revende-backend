---
tags: [caso-de-uso, marketplace]
endpoint: GET /api/listings
auth: público
---

# UC-06 — Listar anúncios ativos

**Ator:** qualquer pessoa, sem login · **Objetivo:** ver ingressos à venda. É a **vitrine**,
a tela mais importante do produto.

## Fluxo principal

1. Opcionalmente informa `?eventId=`.
2. Sistema busca anúncios com status `ATIVO` (do evento, se filtrado).
3. Responde **200** com a lista, cada item já com o [[Evento]] embutido e
   `sellerId` + `sellerName`.

## Regras

[[RN-008 Vitrine mostra apenas anúncios ativos]] · [[RN-005 Anúncio nasce ativo]]

## Dívidas

- **N+1 garantido**: `event` e `seller` são `EAGER`; N anúncios → consultas extras.
  Precisa de `JOIN FETCH` ou projeção. Dívida nº 6 em [[Dívidas Técnicas]].
- Sem paginação e sem ordenação definida — nem por preço, nem por data do evento.
- `eventId` inexistente devolve lista vazia, não 404. Aceitável, mas indistinguível
  de "evento sem anúncios".

## Privacidade

A resposta expõe `sellerId` e `sellerName`. **Não** expõe e-mail nem telefone — e é assim
que deve continuar até haver decisão sobre contato ([[RN-014 Plataforma não intermedia pagamento]]).

## Código

`ListingController.list` → `ListingService.listActive`.

## Testes

só ativos aparecem · filtro por evento · resposta não contém e-mail/telefone/senha ·
contagem de queries não cresce com o número de anúncios.
