---
tags: [regra-de-negocio, marketplace, implementada, fronteira]
contexto: Marketplace
codigo: src/main/java/com/revende/backend/controller/ListingController.java
---

# RN-014 — Plataforma não intermedia pagamento

> O Revende hoje é **vitrine, não transação**. Não há pagamento, pedido, reserva, entrega
> de ingresso nem canal de contato. A venda acontece fora da plataforma e o vendedor apenas
> **declara** que ocorreu.

## Por que isso importa mais que parece

É a regra que define o que o produto **é**. Enquanto valer:

- não existe comprador como entidade — ver [[Glossário]];
- `VENDIDO` é declaração unilateral e não auditável do vendedor
  ([[UC-10 Marcar anúncio como vendido]]);
- não há como cobrar taxa, garantir entrega, nem mediar disputa;
- não há proteção contra vender o mesmo ingresso duas vezes.

## Como está no código

Por **omissão**: não há entidade `Order`, `Payment` ou `Message`; nenhum endpoint de compra;
`ListingResponse` expõe `sellerId` e `sellerName`, mas **nenhum meio de contato** —
`User.phone` existe no banco e não é exposto em resposta alguma.

> [!question] Furo de produto
> Se não há contato exposto e não há mensageria, **como o comprador chega ao vendedor?**
> Não há resposta no código. É a lacuna mais urgente do domínio — ver [[Perguntas em Aberto]].

## O que muda quando cair

Virar transacional não é "adicionar um endpoint": muda o domínio inteiro.
Entra `RESERVADO` em [[Status do Anúncio]], entra o agregado `Order`, `VENDIDO` deixa de ser
declarado por um lado só, e aparecem estorno, prazo e disputa.
**Exige ADR antes de qualquer linha de código** — ver [[Índice de Decisões]].

Ver [[Contexto Marketplace]] e [[Anúncio de Ingresso]].
