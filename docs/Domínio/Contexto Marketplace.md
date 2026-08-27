---
tags: [dominio, contexto, marketplace]
pacote-alvo: com.revende.backend.marketplace
---

# Contexto Marketplace

O coração do produto: **quem revende o quê, por quanto, e em que situação está a oferta**.

## Fronteira

Dentro: publicação, vitrine, ciclo de vida e posse do anúncio.
Fora: identidade do vendedor ([[Contexto Identity]]) e dados do show ([[Contexto Catalog]]),
ambos referenciados por ID.

## Modelo

- [[Anúncio de Ingresso]] — agregado raiz
- [[Status do Anúncio]] · [[Tipo de Ingresso]] — value objects do agregado

## Regras

- [[RN-004 Preço de revenda é livre]]
- [[RN-005 Anúncio nasce ativo]]
- [[RN-006 Apenas o dono altera o anúncio]]
- [[RN-007 Ciclo de vida do anúncio]]
- [[RN-008 Vitrine mostra apenas anúncios ativos]]
- [[RN-009 Lote de ingressos é indivisível]]
- [[RN-011 Anúncio exige evento existente]]
- [[RN-012 Cancelamento é lógico]]
- [[RN-014 Plataforma não intermedia pagamento]]

## Casos de uso

- [[UC-06 Listar anúncios ativos]] · [[UC-07 Detalhar anúncio]] · [[UC-08 Listar meus anúncios]]
- [[UC-09 Publicar anúncio]] · [[UC-10 Marcar anúncio como vendido]] · [[UC-11 Cancelar anúncio]]

## Código hoje

`model/TicketListing`, `model/ListingStatus`, `model/TicketType`,
`repository/TicketListingRepository`, `service/ListingService`, `controller/ListingController`.

## Observações

É o contexto mais rico e o **primeiro a migrar** para hexagonal, por servir de exemplo
aos demais — ver [[Mapa de Migração]] e [[ADR-0001 Adotar arquitetura hexagonal]].
Hoje toda a regra vive em `ListingService`, num modelo anêmico com setters públicos.
