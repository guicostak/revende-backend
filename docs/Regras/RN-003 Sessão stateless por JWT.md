---
tags: [regra-de-negocio, identity, seguranca, parcial]
contexto: Identity
codigo: src/main/java/com/revende/backend/security/JwtService.java
---

# RN-003 — Sessão stateless por JWT

> A autenticação é feita por token JWT assinado, válido por **24 horas**, enviado em
> `Authorization: Bearer <token>`. O servidor não guarda sessão.

## Por quê

API consumida por front separado (`localhost:3000` no CORS) e horizontalmente escalável.
Ver [[ADR-0002 JWT stateless sem refresh token]].

## Implementação

- `JwtService` assina com HMAC-SHA (`Keys.hmacShaKeyFor`), `subject` = e-mail do usuário,
  `expiration` = `revende.jwt.expiration-ms` (86 400 000 ms = 24 h).
- `JwtAuthFilter` extrai o token, recarrega o `UserDetails` do banco e popula o
  `SecurityContext`. Sessão `STATELESS` no `SecurityConfig`.

## Furos

1. **O segredo está versionado** em `application.yml` — qualquer um com acesso ao repo
   forja tokens de qualquer usuário. Dívida nº 2 em [[Dívidas Técnicas]].
2. `JwtAuthFilter` tem `catch (Exception ignored)`: token expirado, assinatura inválida e
   erro de banco viram todos "sem autenticação", sem log. Dívida nº 5.
3. Sem refresh token e sem revogação: um token vazado vale 24 h e não há como invalidá-lo.
4. O token não carrega papel algum — ver [[RN-010 Quem pode cadastrar evento]].

## Testes esperados

- token válido → acessa rota protegida
- token expirado / assinatura adulterada / header ausente → 401
- token de usuário deletado → 401

Aplicada em [[UC-02 Autenticar]] e em toda rota protegida.
