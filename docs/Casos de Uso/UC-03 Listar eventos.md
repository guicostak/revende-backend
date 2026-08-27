---
tags: [caso-de-uso, catalog]
endpoint: GET /api/events
auth: público
---

# UC-03 — Listar eventos

**Ator:** qualquer pessoa, sem login · **Objetivo:** descobrir shows disponíveis.

## Fluxo principal

1. Opcionalmente informa `?city=` ou `?name=`.
2. Sistema aplica **um** filtro (cidade tem precedência) ou devolve tudo.
3. Responde **200** com a lista de [[Evento]].

## Regras

[[RN-013 Busca de eventos por cidade ou nome]]

## Fluxos alternativos

- Sem parâmetro → catálogo inteiro, sem limite (**problema de escala**).
- `city` + `name` → `name` é ignorado silenciosamente.
- Nenhum resultado → `200` com lista vazia (correto, não é 404).

## Dívidas

Sem paginação, sem ordenação definida (a ordem vem do banco, não é garantida),
sem filtro por data — evento que já passou aparece normalmente.

## Código

`EventController.list` → `EventService.list` → `EventRepository`.

## Testes

filtro por cidade case-insensitive · filtro por trecho do nome · sem filtro devolve tudo ·
ordem estável e explícita quando a paginação entrar.
