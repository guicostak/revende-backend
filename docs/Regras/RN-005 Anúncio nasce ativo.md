---
tags: [regra-de-negocio, marketplace, implementada]
contexto: Marketplace
codigo: src/main/java/com/revende/backend/service/ListingService.java
---

# RN-005 — Anúncio nasce ativo

> Todo anúncio publicado entra imediatamente na vitrine com situação `ATIVO`.
> Não há moderação, rascunho ou aprovação prévia.

## Por quê

Atrito zero para publicar. A confiança é otimista: o mercado se autorregula pela reputação
(que ainda não existe — ver [[Perguntas em Aberto]]).

## Implementação

`ListingService.create` faz `listing.setStatus(ListingStatus.ATIVO)` explicitamente, e o
campo já tem `= ListingStatus.ATIVO` como default na entidade. O cliente **não** consegue
escolher o status: `ListingRequest` não tem esse campo. Isso está certo.

## Furos

- Não há moderação nem detecção de anúncio suspeito.
- Nada impede publicar para evento que já aconteceu — ver [[Evento]].

## Alvo hexagonal

O default deixa de ser atributo com valor inicial e passa a ser garantido pela factory do
agregado: `TicketListing.publish(...)` devolve sempre um anúncio ativo, e `status` não tem setter.

## Testes esperados

- publicar → status `ATIVO` na resposta e no banco
- tentar forjar status no corpo do request → campo ignorado

Aplicada em [[UC-09 Publicar anúncio]] · ver [[Status do Anúncio]].
