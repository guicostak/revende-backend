---
tags: [caso-de-uso, marketplace, critico]
endpoint: PATCH /api/listings/{id}/sold
auth: JWT (dono)
---

# UC-10 — Marcar anúncio como vendido

**Ator:** vendedor dono do anúncio · **Objetivo:** declarar a venda e tirar a oferta da vitrine.

## Fluxo principal

1. Envia o token e o `id` do anúncio.
2. Sistema carrega o [[Anúncio de Ingresso]] e confere a posse.
3. Muda a situação para `VENDIDO`.
4. Responde **200** com o anúncio atualizado.

## Regras

[[RN-006 Apenas o dono altera o anúncio]] · [[RN-007 Ciclo de vida do anúncio]] ·
[[RN-014 Plataforma não intermedia pagamento]]

## 🐞 Dois furos confirmados

1. **Não-dono recebe 400 em vez de 403.** `ensureOwner` lança `IllegalArgumentException`.
2. **Transição não é validada.** Um anúncio `CANCELADO` pode ser marcado como `VENDIDO`;
   um `VENDIDO` pode ser marcado de novo. Deveria ser **409**.

## Natureza da declaração

Como a plataforma não intermedia nada, "vendido" é **afirmação unilateral do vendedor**,
sem contraparte e sem prova. Nenhuma métrica de GMV, taxa ou reputação pode se apoiar
nesse dado enquanto [[RN-014 Plataforma não intermedia pagamento]] valer.

## Erros

| Situação | Hoje | Deveria ser |
|---|---|---|
| Não é o dono | 400 | **403** |
| Já vendido / já cancelado | 200, sobrescreve | **409** |
| Anúncio inexistente | 404 ✅ | |
| Sem token | 401 ✅ | |

## Código

`ListingController.markSold` → `ListingService.markSold` → `ensureOwner`.

## Testes

dono → 200 · **outro usuário → 403 e nada muda** · cancelado → vendido → **409** ·
vendido → vendido → **409** · some da vitrine depois.
