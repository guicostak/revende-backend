---
tags: [regra-de-negocio, identity, seguranca, implementada]
contexto: Identity
codigo: src/main/java/com/revende/backend/service/AuthService.java
---

# RN-002 — Senha mínima e hash BCrypt

> A senha tem no mínimo **6 caracteres** e nunca é armazenada em claro.

## Por quê

Piso mínimo de segurança da conta. O hash protege os usuários caso o banco vaze —
inclusive contra nós mesmos.

## Implementação

`@Size(min = 6)` em `RegisterRequest.password` (violação → 400 com mapa de campos).
`BCryptPasswordEncoder` (força padrão 10) aplicado em `AuthService.register`.
A comparação no login é feita pelo `AuthenticationManager`, nunca manualmente.

## Furos

- 6 caracteres é fraco. Não há checagem de senha vazada nem de complexidade.
- Não há troca de senha, recuperação, nem expiração.
- `LoginRequest.password` só exige `@NotBlank` — correto: a regra de força vale no cadastro.

## Invariantes

- `User.password` **nunca** aparece em resposta, log ou mensagem de erro.
- Nenhum código fora do Identity toca no campo.

## Testes esperados

- senha com 5 caracteres → 400 com o campo `password` no corpo
- senha válida → hash gravado difere do texto e valida com `matches`

Aplicada em [[UC-01 Registrar conta]] e [[UC-02 Autenticar]].
