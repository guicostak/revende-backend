---
tags: [regra-de-negocio, marketplace, parcial]
contexto: Marketplace
codigo: src/main/java/com/revende/backend/service/ListingService.java
---

# RN-008 — Vitrine mostra apenas anúncios ativos

> A listagem pública devolve **somente** anúncios `ATIVO`. Vendidos e cancelados saem da
> vitrine. A listagem é aberta: navegar não exige login.

## Por quê

Vitrine é promessa de disponibilidade. Mostrar oferta morta gera contato frustrado.
O acesso anônimo existe para o funil: a pessoa vê antes de decidir criar conta.

## Implementação

`ListingService.listActive` filtra por `ListingStatus.ATIVO`, com ou sem `eventId`.
`SecurityConfig` libera `GET /api/listings/**` sem token.

## Furos

1. **`GET /api/listings/{id}` não filtra status** — buscando o ID direto, qualquer um vê
   anúncio vendido ou cancelado, inclusive de outra pessoa. Decidir: 404 para não-ativo,
   ou exibir com o status marcado. Precisa de decisão de produto.
2. `GET /api/listings/me` casa com o padrão `/api/listings/**` liberado, mas exige
   `Authentication` no controller — sem token dá **NPE → 500**, quando deveria ser 401.
   Regra do [[CLAUDE]]: caminho de erro tem status correto.
3. Sem paginação: cresceu, devolve tudo. Dívida nº 8 em [[Dívidas Técnicas]].

## Testes esperados

- anúncio vendido/cancelado não aparece em `GET /api/listings`
- filtro `?eventId=` devolve só ativos daquele evento
- `GET /api/listings/me` sem token → 401 (hoje 500)

Aplicada em [[UC-06 Listar anúncios ativos]] e [[UC-07 Detalhar anúncio]].
