---
tags: [dominio, contexto, catalog]
pacote-alvo: com.revende.backend.catalog
---

# Contexto Catalog

O catálogo de **eventos que existem no mundo**. É a referência estável sobre a qual
os anúncios são pendurados.

## Fronteira

Dentro: dados do evento (nome, data, local, cidade, categoria, imagem) e sua busca.
Fora: qualquer coisa sobre ingressos ou preço — isso é [[Contexto Marketplace]].

## Modelo

- [[Evento]] — único agregado.

## Regras

- [[RN-010 Quem pode cadastrar evento]]
- [[RN-013 Busca de eventos por cidade ou nome]]

## Casos de uso

- [[UC-03 Listar eventos]] · [[UC-04 Detalhar evento]] · [[UC-05 Cadastrar evento]]

## Código hoje

`model/Event`, `repository/EventRepository`, `service/EventService`, `controller/EventController`.

## Observações

`EventService.findEntity(Long)` é chamado direto por `ListingService` — é um **vazamento de
fronteira**: o Marketplace importa uma classe do Catalog e recebe uma entidade JPA de volta.
No alvo hexagonal isso vira um port de saída (`LoadEventPort`) do Marketplace, implementado
por um adapter que fala com o Catalog. Ver [[Mapa de Migração]].
