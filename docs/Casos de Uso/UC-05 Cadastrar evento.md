---
tags: [caso-de-uso, catalog, atencao]
endpoint: POST /api/events
auth: JWT
---

# UC-05 — Cadastrar evento

**Ator:** qualquer usuário autenticado · **Objetivo:** colocar um show no catálogo para
poder anunciar ingressos dele.

## Fluxo principal

1. Envia `name`, `date`, `venue`, `city` (obrigatórios) + `description`, `category`, `imageUrl`.
2. Sistema valida e grava.
3. Responde **201** com o evento criado.

## Regras

[[RN-010 Quem pode cadastrar evento]] ⚠️

## ⚠️ Lacuna de autorização

Não há papel: **qualquer conta criada agora cadastra evento**. O controller nem recebe
`Authentication`, então **a autoria não é registrada**. Sem chave natural, duplicatas passam.

Enquanto [[RN-010 Quem pode cadastrar evento]] não for decidida, trate este endpoint como
provisório e não construa nada que dependa de catálogo confiável.

## Erros

| Situação | Hoje |
|---|---|
| Campo obrigatório faltando | 400 com mapa `fields` ✅ |
| Sem token | 401 ✅ |
| Data no passado | **aceito** — não há validação |
| Evento duplicado | **aceito** — não há chave natural |

## Código

`EventController.create` → `EventService.create`.

## Testes

sem token → 401 · payload mínimo válido → 201 · campo obrigatório ausente → 400 ·
(quando a regra existir) usuário sem papel → 403.
