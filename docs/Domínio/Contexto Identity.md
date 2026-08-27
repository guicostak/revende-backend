---
tags: [dominio, contexto, identity]
pacote-alvo: com.revende.backend.identity
---

# Contexto Identity

Responde a duas perguntas: **quem é essa pessoa** e **ela pode fazer isso**.

## Fronteira

Dentro: cadastro de conta, credenciais, emissão e validação de token.
Fora: o que a pessoa vende ([[Contexto Marketplace]]) e o que existe para vender ([[Contexto Catalog]]).

## Modelo

- [[Usuário]] — único agregado.

## Regras

- [[RN-001 E-mail único por conta]]
- [[RN-002 Senha mínima e hash BCrypt]]
- [[RN-003 Sessão stateless por JWT]]

## Casos de uso

- [[UC-01 Registrar conta]]
- [[UC-02 Autenticar]]

## Código hoje

`service/AuthService`, `security/JwtService`, `security/JwtAuthFilter`,
`security/CustomUserDetailsService`, `config/SecurityConfig`.

## Observações

Não existe **papel/role**: todo usuário autenticado tem exatamente os mesmos poderes.
É a raiz de [[RN-010 Quem pode cadastrar evento]] e da dívida nº 9 em [[Dívidas Técnicas]].

Os outros contextos identificam a pessoa por **e-mail em `String`**, vindo de
`Authentication.getName()`. Ver [[CLAUDE]] §1.4 item 6: o alvo é um `UserId` tipado,
publicado como port deste contexto.
