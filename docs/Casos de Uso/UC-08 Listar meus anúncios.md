---
tags: [caso-de-uso, marketplace]
endpoint: GET /api/listings/me
auth: JWT
---

# UC-08 — Listar meus anúncios

**Ator:** vendedor autenticado · **Objetivo:** gerenciar as próprias ofertas.

## Fluxo principal

1. Envia o token.
2. Sistema resolve o [[Usuário]] pelo e-mail do `Authentication` e busca por `sellerId`.
3. Responde **200** com **todos** os anúncios do vendedor — ativos, vendidos e cancelados.

## Regra implícita, importante

Diferente da vitrine ([[RN-008 Vitrine mostra apenas anúncios ativos]]), aqui o histórico
completo **aparece de propósito**: o vendedor precisa ver o que já vendeu e cancelou.
Isso é regra, não acidente — e não estava escrito em lugar nenhum antes desta nota.

## 🐞 Furo confirmado

A rota casa com o padrão público `GET /api/listings/**` no `SecurityConfig`. Sem token,
`Authentication` chega **nulo** e `auth.getName()` estoura **NPE → 500**, em vez de 401.
Corrigir liberando explicitamente só `GET /api/listings` e `/api/listings/{id}`,
ou exigindo autenticação para `/me` antes do padrão coringa.

## Erros

| Situação | Hoje | Deveria ser |
|---|---|---|
| Sem token | 500 | **401** |
| Token de usuário deletado | `NotFoundException` → 404 | 401 |

## Código

`ListingController.myListings` → `ListingService.myListings(email)`.

## Testes

sem token → 401 · com token → só os anúncios daquele vendedor, **em qualquer status** ·
nunca vaza anúncio de terceiro.
