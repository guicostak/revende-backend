---
tags: [caso-de-uso, marketplace]
endpoint: DELETE /api/listings/{id}
auth: JWT (dono)
---

# UC-11 — Cancelar anúncio

**Ator:** vendedor dono do anúncio · **Objetivo:** desistir da oferta.

## Fluxo principal

1. Envia o token e o `id`.
2. Sistema confere a posse e muda a situação para `CANCELADO`.
3. Responde **204 No Content**.

## Regras

[[RN-006 Apenas o dono altera o anúncio]] · [[RN-007 Ciclo de vida do anúncio]] ·
[[RN-012 Cancelamento é lógico]]

## Importante

`DELETE` **não apaga nada**: o registro fica no banco com status `CANCELADO`, por decisão
consciente ([[ADR-0003 Cancelamento lógico de anúncio]]). Nunca troque isso por um
`repository.delete()` — o histórico é ativo do produto, e há implicação de LGPD no caminho.

## Erros

| Situação | Hoje | Deveria ser |
|---|---|---|
| Não é o dono | 400 | **403** |
| Já cancelado | 204, regrava | **409** (ou 204 idempotente — decidir) |
| Já vendido | 204, **apaga o registro da venda** | **409** |
| Anúncio inexistente | 404 ✅ | |

> [!warning] Cancelar um anúncio já vendido destrói o histórico da venda.
> É o mesmo furo de [[RN-007 Ciclo de vida do anúncio]], visto do outro lado.

## Código

`ListingController.cancel` → `ListingService.cancel`.

## Testes

dono cancela → 204 e **registro continua existindo** com status `CANCELADO` ·
outro usuário → 403 · anúncio vendido → 409 · some da vitrine.
