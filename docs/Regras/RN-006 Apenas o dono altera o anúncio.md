---
tags: [regra-de-negocio, marketplace, seguranca, parcial]
contexto: Marketplace
codigo: src/main/java/com/revende/backend/service/ListingService.java
---

# RN-006 — Apenas o dono altera o anúncio

> Marcar como vendido e cancelar são exclusivos do **vendedor que publicou** o anúncio.
> Qualquer outro usuário autenticado é recusado.

## Por quê

É a regra de posse do [[Contexto Marketplace]]. Sem ela, qualquer pessoa logada sabota
os anúncios alheios — sabotagem de concorrente ou vandalismo puro.

## Implementação

`ListingService.ensureOwner` compara `listing.getSeller().getEmail()` com o e-mail do
`Authentication`, e lança `IllegalArgumentException("Você não é o dono deste anúncio")`.

## 🐞 Furo confirmado

O `GlobalExceptionHandler` mapeia `IllegalArgumentException` → **HTTP 400 (Bad Request)**.
Negar permissão é **403 (Forbidden)**. Consequências reais:

- o front não distingue "dado inválido" de "não é seu";
- monitoramento não consegue alertar sobre tentativas de acesso indevido;
- viola [[Qualidade de Código]] §segurança: *autorização é decisão de domínio*.

Correção: exceção de domínio própria (`NotListingOwnerException`) com handler dedicado → 403.
Dívida nº 4 em [[Dívidas Técnicas]].

## Furo secundário

A comparação é por **e-mail em String**. Deve ser por `SellerId` tipado — [[CLAUDE]] §1.4 item 6.

## Testes esperados

- dono marca vendido → 200
- **outro usuário marca vendido → 403 e o anúncio não muda**
- outro usuário cancela → 403
- sem token → 401

Aplicada em [[UC-10 Marcar anúncio como vendido]] e [[UC-11 Cancelar anúncio]].
