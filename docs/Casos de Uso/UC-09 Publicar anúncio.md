---
tags: [caso-de-uso, marketplace, critico]
endpoint: POST /api/listings
auth: JWT
---

# UC-09 — Publicar anúncio

**Ator:** vendedor autenticado · **Objetivo:** colocar ingressos à venda.
É o caso de uso central do produto.

## Pré-condições

Usuário autenticado ([[RN-003 Sessão stateless por JWT]]) e [[Evento]] já cadastrado
([[RN-011 Anúncio exige evento existente]]).

## Fluxo principal

1. Envia `eventId`, `ticketType`, `originalPrice`, `price`, `quantity`, `description`.
2. Sistema resolve o vendedor pelo token e carrega o evento.
3. Cria o [[Anúncio de Ingresso]] com status `ATIVO`.
4. Responde **201** com o anúncio, evento embutido e dados públicos do vendedor.

## Regras aplicadas

[[RN-004 Preço de revenda é livre]] · [[RN-005 Anúncio nasce ativo]] ·
[[RN-009 Lote de ingressos é indivisível]] · [[RN-011 Anúncio exige evento existente]]

## Erros

| Situação | Hoje | Deveria ser |
|---|---|---|
| Sem token | 401 ✅ | |
| `eventId` inexistente | 404 ✅ | |
| Preço ou quantidade ≤ 0 | 400 ✅ | |
| `ticketType` inválido | erro de desserialização → 400/500 inconsistente | 400 com o valor aceito |
| Evento já ocorrido | **aceito** | recusar (regra a decidir) |

## Alvo hexagonal

```
PublishListingUseCase (port in)
  ← ListingController
  → LoadEventPort, SaveListingPort, LoadSellerPort (ports out)
  → TicketListing.publish(...)   // invariantes no domínio
```

Sem `@Transactional` hoje: criar e salvar rodam fora de transação explícita.
Ver [[CLAUDE]] §2.3 e [[Dívidas Técnicas]].

## Testes

publicação válida → 201 e status `ATIVO` · evento inexistente → 404 · preço negativo → 400 ·
sem token → 401 · o vendedor gravado é **o do token**, nunca um id vindo do corpo.
