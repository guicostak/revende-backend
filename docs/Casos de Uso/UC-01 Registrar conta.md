---
tags: [caso-de-uso, identity]
endpoint: POST /api/auth/register
auth: público
---

# UC-01 — Registrar conta

**Ator:** visitante · **Objetivo:** ter conta para publicar anúncios.

## Fluxo principal

1. Envia `name`, `email`, `password` e `phone` (opcional).
2. Sistema valida o formato e recusa e-mail já cadastrado.
3. Gera o hash da senha, grava o [[Usuário]] e emite token.
4. Responde **201** com `token`, `userId`, `name`, `email`.

## Regras

[[RN-001 E-mail único por conta]] · [[RN-002 Senha mínima e hash BCrypt]] ·
[[RN-003 Sessão stateless por JWT]]

## Erros

| Situação | Hoje | Deveria ser |
|---|---|---|
| Campo inválido / senha < 6 | 400 com mapa `fields` | ✅ igual |
| E-mail já cadastrado | 400 | **409** |
| Corrida entre dois cadastros iguais | 500 | **409** |

## Código

`AuthController.register` → `AuthService.register` → `UserRepository` + `JwtService`.

## Alvo hexagonal

`RegisterUserUseCase` (port in) · `SaveUserPort`, `CheckEmailAvailabilityPort`,
`IssueTokenPort` (ports out). Ver [[Arquitetura Hexagonal]].

## Testes

cadastro válido → 201 e senha gravada como hash · e-mail duplicado → 409 e nenhuma conta nova ·
senha curta → 400 apontando o campo · resposta **nunca** contém a senha.
