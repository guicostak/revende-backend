---
tags: [caso-de-uso, identity]
endpoint: POST /api/auth/login
auth: público
---

# UC-02 — Autenticar

**Ator:** usuário cadastrado · **Objetivo:** obter token para operar.

## Fluxo principal

1. Envia `email` e `password`.
2. `AuthenticationManager` confere as credenciais contra o hash BCrypt.
3. Sistema emite JWT de 24 h com o e-mail no `subject`.
4. Responde **200** com `token`, `userId`, `name`, `email`.

## Regras

[[RN-002 Senha mínima e hash BCrypt]] · [[RN-003 Sessão stateless por JWT]]

## Erros

| Situação | Hoje | Deveria ser |
|---|---|---|
| Senha errada / usuário inexistente | `BadCredentialsException` sem handler → **500** | **401** com mensagem genérica |
| E-mail malformado | 400 | ✅ igual |

> [!bug] Furo confirmado
> O `GlobalExceptionHandler` **não trata `AuthenticationException`**. Login com senha errada
> — o erro mais comum da aplicação inteira — devolve 500. Precisa de handler dedicado
> respondendo 401 com texto genérico ("credenciais inválidas"), sem revelar se o e-mail existe.

## Código

`AuthController.login` → `AuthService.login`.

## Testes

credenciais válidas → 200 com token utilizável · senha errada → 401 · e-mail inexistente → 401
**com a mesma mensagem** da senha errada (não vazar existência de conta).
