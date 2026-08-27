---
tags: [regra-de-negocio, marketplace, implementada]
contexto: Marketplace
codigo: src/main/java/com/revende/backend/dto/ListingDtos.java
---

# RN-009 — Lote de ingressos é indivisível

> Um anúncio representa um **lote** de N ingressos (N ≥ 1) vendido em bloco.
> Não há compra parcial: ou leva o lote inteiro, ou nada.

## Por quê

Simplifica o domínio: o [[Anúncio de Ingresso]] tem um único ciclo de vida
([[RN-007 Ciclo de vida do anúncio]]). Venda parcial exigiria estado intermediário,
reserva e decremento de estoque — nada disso existe.
Também casa com a realidade: quem tem 2 ingressos costuma querer vender o par.

## Implementação

`@NotNull @Positive Integer quantity` no `ListingRequest`; default `1` na entidade.
`quantity` **nunca é decrementada** — a venda muda o status do lote todo.

## Consequências

- `price` é o preço **do lote**, não unitário. Isso não está explícito em lugar nenhum da
  API nem da UI — fonte provável de confusão. Vale renomear para `lotPrice` ou expor
  `pricePerTicket` calculado.
- Quem quer vender 2 de 4 ingressos precisa criar dois anúncios.

## Testes esperados

- `quantity = 0` ou negativo → 400
- `quantity` ausente → assume 1? **Não**: hoje `@NotNull` recusa. Confirmado como intencional.

Aplicada em [[UC-09 Publicar anúncio]] · ver [[Perguntas em Aberto]].
