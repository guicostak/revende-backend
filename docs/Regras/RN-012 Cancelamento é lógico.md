---
tags: [regra-de-negocio, marketplace, implementada]
contexto: Marketplace
codigo: src/main/java/com/revende/backend/service/ListingService.java
---

# RN-012 — Cancelamento é lógico

> `DELETE /api/listings/{id}` **não apaga** o anúncio: muda a situação para `CANCELADO`.
> O registro permanece no banco para sempre.

## Por quê

Histórico é ativo do marketplace: base para reputação futura, resolução de disputa e
detecção de fraude (ex.: vendedor que publica e cancela em série). Apagar destrói prova.

Ver [[ADR-0003 Cancelamento lógico de anúncio]].

## Implementação

`ListingService.cancel` faz `setStatus(CANCELADO)` e salva. O controller devolve **204**.
Nenhum `delete` é chamado em nenhum ponto do código.

## Atenção

O verbo HTTP `DELETE` com semântica de *cancelar* é uma mentira leve, mas aceitável e
comum. O que **não** é aceitável é o código chamar isso de "deletar" — ver [[Glossário]],
termos proibidos.

## Consequências

- Anúncio cancelado continua acessível por `GET /api/listings/{id}` — ver furo em
  [[RN-008 Vitrine mostra apenas anúncios ativos]].
- LGPD: se o usuário pedir exclusão da conta, os anúncios precisam ser anonimizados,
  não apagados. **Não há tratamento disso hoje.**

## Testes esperados

- cancelar → 204 e registro ainda existe com status `CANCELADO`
- contagem de registros na tabela não diminui

Aplicada em [[UC-11 Cancelar anúncio]].
