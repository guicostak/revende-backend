---
tags: [regra-de-negocio, identity, implementada]
contexto: Identity
codigo: src/main/java/com/revende/backend/service/AuthService.java
---

# RN-001 — E-mail único por conta

> Cada e-mail identifica **uma** conta. Tentar cadastrar um e-mail já existente é recusado.

## Por quê

O e-mail é a identidade de negócio do [[Usuário]] e o `subject` do token
([[RN-003 Sessão stateless por JWT]]). Duplicidade tornaria o login ambíguo.

## Implementação

`AuthService.register` chama `userRepository.existsByEmail` e lança
`IllegalArgumentException("E-mail já cadastrado")` → **HTTP 400**.
Reforçado no banco por `@Column(unique = true)` em `User.email`.

## Furos

- Semanticamente é **conflito (409)**, não requisição malformada (400).
- Corrida entre duas requisições simultâneas passa pelo `existsByEmail` e estoura
  `DataIntegrityViolationException` → hoje vira **500**. A constraint do banco é a
  verdade; o handler precisa traduzi-la.
- Não há normalização: `Maria@x.com` e `maria@x.com` criam contas distintas.
- Não há confirmação de e-mail — ver [[Perguntas em Aberto]].

## Testes esperados

- registrar e-mail novo → 201 com token
- registrar e-mail existente → 409 e nenhuma conta criada
- variação de maiúsculas do mesmo e-mail → recusado

Aplicada em [[UC-01 Registrar conta]].
