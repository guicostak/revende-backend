---
tags: [regra-de-negocio, marketplace, implementada]
contexto: Marketplace
codigo: src/main/java/com/revende/backend/service/ListingService.java
---

# RN-011 — Anúncio exige evento existente

> Todo anúncio aponta para um [[Evento]] já cadastrado. Publicar para evento inexistente
> é recusado com **404**.

## Por quê

O evento é a âncora de busca do comprador e a garantia de que a oferta é sobre algo real.
Anúncio órfão é invisível e insustentável.

## Implementação

`ListingService.create` chama `eventService.findEntity(req.eventId())`, que lança
`NotFoundException("Evento não encontrado: id")` → **404** pelo `GlobalExceptionHandler`.
Reforçado no banco por `@ManyToOne(optional = false)`.

## Furo de arquitetura

O Marketplace chama **direto** um service do Catalog e recebe de volta uma entidade JPA.
Isso amarra os dois contextos e impede evoluir um sem o outro. No alvo hexagonal:

```java
// marketplace/application/port/out
public interface LoadEventPort { Optional<EventSnapshot> byId(EventId id); }
```

O Marketplace passa a depender de um **snapshot imutável** do evento, não da entidade viva.
Ver [[CLAUDE]] §1.4 item 7 e [[Contexto Catalog]].

## Furos de negócio

- Não valida se o evento **já aconteceu**.
- Não impede o mesmo vendedor de publicar dezenas de anúncios iguais para o mesmo evento.

## Testes esperados

- `eventId` inexistente → 404, nada gravado
- `eventId` nulo → 400 (validação)
- evento válido → 201 com o evento embutido na resposta

Aplicada em [[UC-09 Publicar anúncio]].
