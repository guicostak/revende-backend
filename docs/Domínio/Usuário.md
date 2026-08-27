---
tags: [dominio, agregado, identity]
contexto: Identity
codigo: src/main/java/com/revende/backend/model/User.java
---

# Usuário

Pessoa com conta no Revende. Contexto: [[Contexto Identity]].

## Atributos

| Campo | Tipo | Obrigatório | Nota |
|---|---|---|---|
| `id` | Long | gerado | identidade técnica |
| `name` | String | sim | nome de exibição, sem unicidade |
| `email` | String | sim, **único** | identidade de negócio — ver [[RN-001 E-mail único por conta]] |
| `password` | String | sim | hash BCrypt, mín. 6 chars em claro — [[RN-002 Senha mínima e hash BCrypt]] |
| `phone` | String | não | **não é exposto em nenhuma resposta da API hoje** |
| `createdAt` | Instant | automático | `Instant.now()` no campo |

## Comportamento

Nenhum. É um bean anêmico com getters e setters públicos — qualquer código pode fazer
`user.setEmail(...)` e furar a invariante de unicidade.

**Alvo:** entidade de domínio imutável, criada por factory (`User.register(...)`), com
`EmailAddress` e `PasswordHash` como value objects, separada de `UserJpaEntity`.

## Relações

- É o **vendedor** de zero ou mais [[Anúncio de Ingresso]] (`TicketListing.seller`).
- Não tem papel/role — ver [[Contexto Identity]] e [[RN-010 Quem pode cadastrar evento]].

## Exposição

`AuthResponse` devolve `token`, `userId`, `name`, `email`.
`ListingResponse` expõe `sellerId` e `sellerName` — **nunca** e-mail, telefone ou hash.
Ver [[Qualidade de Código]] §segurança.
