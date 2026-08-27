---
tags: [caso-de-uso, catalog]
endpoint: GET /api/events/{id}
auth: público
---

# UC-04 — Detalhar evento

**Ator:** qualquer pessoa · **Objetivo:** ver os dados completos de um [[Evento]].

## Fluxo principal

1. Informa o `id` na URL.
2. Sistema busca; se não achar, lança `NotFoundException`.
3. Responde **200** com o evento.

## Erros

| Situação | Resposta |
|---|---|
| ID inexistente | **404** ✅ correto |
| ID não numérico | `MethodArgumentTypeMismatchException` sem handler → **500** (deveria ser 400) |

## Observação

A resposta **não traz os anúncios daquele evento**, o que obriga o front a uma segunda
chamada (`GET /api/listings?eventId=`). É uma decisão implícita de manter os contextos
separados na API — coerente com [[Contexto Catalog]] e [[Contexto Marketplace]],
mas vale documentar como decisão consciente.

## Código

`EventController.get` → `EventService.get` → `findEntity`.

## Testes

id existente → 200 · id inexistente → 404 com mensagem · id inválido → 400.
