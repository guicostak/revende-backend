---
tags: [caso-de-uso, marketplace]
endpoint: GET /api/listings/{id}
auth: público
---

# UC-07 — Detalhar anúncio

**Ator:** qualquer pessoa · **Objetivo:** ver os detalhes de uma oferta.

## Fluxo principal

1. Informa o `id`.
2. Sistema busca **sem filtrar status**.
3. Responde **200** com o [[Anúncio de Ingresso]] completo.

## 🐞 Decisão pendente

Buscando o ID direto, qualquer pessoa vê anúncio **vendido ou cancelado**. Isso contradiz
o espírito de [[RN-008 Vitrine mostra apenas anúncios ativos]].

Duas saídas, ambas defensáveis — **precisa de decisão de produto**:
- **404** para não-ativo (vitrine é só o que está vivo);
- **200 com o status visível**, para o link compartilhado não morrer.

O que não pode continuar é ser acidente de implementação.

## Erros

| Situação | Resposta |
|---|---|
| ID inexistente | 404 ✅ |
| Anúncio cancelado/vendido | 200 (comportamento não decidido) |

## Código

`ListingController.get` → `ListingService.get` → `findEntity`.

## Testes

id inexistente → 404 · anúncio ativo → 200 · anúncio cancelado → comportamento decidido e travado por teste.
