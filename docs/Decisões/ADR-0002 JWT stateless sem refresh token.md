---
tags: [adr, seguranca, identity]
status: aceita-com-ressalva
data: 2026-08-27
---

# ADR-0002 — JWT stateless sem refresh token

**Status:** Aceita com ressalva · **Data:** 2026-08-27

## Contexto

A API é consumida por um front separado (CORS aberto para `localhost:3000`) e precisa
escalar horizontalmente sem sessão compartilhada.

## Decisão

Autenticação por **JWT assinado (HMAC-SHA), validade de 24 h, `subject` = e-mail**, sessão
`STATELESS`, sem refresh token e sem lista de revogação. Ver [[RN-003 Sessão stateless por JWT]].

## Ressalvas — condições para continuar valendo

Esta decisão **só é aceitável** enquanto as três forem verdade:

1. **O segredo sai do repositório** e passa a vir de variável de ambiente. Hoje está
   versionado em `application.yml` — enquanto estiver, qualquer pessoa com acesso ao código
   forja token de qualquer usuário. É bloqueador para produção.
2. O sistema **não movimenta dinheiro** ([[RN-014 Plataforma não intermedia pagamento]]).
   Se a plataforma passar a intermediar pagamento, 24 h sem revogação deixa de ser aceitável.
3. Falha de token é **logada**, não engolida (`catch (Exception ignored)` hoje em `JwtAuthFilter`).

## Consequências

Servidor sem estado e escala trivial; em troca, **token vazado vale 24 h e não há como
invalidá-lo** — nem no logout, nem na troca de senha, nem no banimento de conta.

## Alternativas descartadas

- **Sessão em servidor** — exige store compartilhado, contraria o front separado.
- **Access + refresh token curto** — a escolha certa quando (2) cair; adiada por custo agora.
