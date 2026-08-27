---
tags: [arquitetura, plano]
---

# Mapa de Migração

Como sair das camadas atuais para [[Arquitetura Hexagonal]] **sem parar o desenvolvimento**.
Regra geral: um contexto por vez, código novo já nasce no formato novo.

## Ordem

1. **[[Contexto Marketplace]]** — mais rico em regra, serve de referência aos outros
2. **[[Contexto Catalog]]** — pequeno, valida o padrão de port entre contextos
3. **[[Contexto Identity]]** — último: mexe em segurança, exige mais teste antes

## Passo a passo do Marketplace

| # | Passo | Entrega |
|---|---|---|
| 1 | ArchUnit + testes de caracterização dos endpoints atuais | rede de segurança antes de mover qualquer coisa |
| 2 | `domain/model/TicketListing` puro, com `publish`, `markSoldBy`, `cancelBy` | [[RN-005 Anúncio nasce ativo]], [[RN-006 Apenas o dono altera o anúncio]], [[RN-007 Ciclo de vida do anúncio]] no domínio, com teste unitário |
| 3 | `TicketListingJpaEntity` + mapper + `ListingPersistenceAdapter` | entidade JPA separada do domínio |
| 4 | Ports out (`LoadListingPort`, `SaveListingPort`, `LoadEventPort`) | `application` deixa de ver `JpaRepository` |
| 5 | Um service por caso de uso, implementando o port in | `ListingService` deixa de ser classe-balde |
| 6 | Controller passa a depender das interfaces; exceções de domínio → 403/409 | corrige os furos de [[UC-10 Marcar anúncio como vendido]] e [[UC-11 Cancelar anúncio]] |

Tabela arquivo-a-arquivo em [[CLAUDE]] §1.5.

## Regras da travessia

- **Nunca duas estruturas para o mesmo contexto ao mesmo tempo.** Terminou de mover, apagou o antigo.
- Cada passo mantém a API pública idêntica — exceto onde a correção de status HTTP é justamente o objetivo.
- Nenhum passo entra sem teste; o ArchUnit do passo 1 é o que impede a regressão silenciosa.

## Sinal de que deu certo

Abrir `marketplace/domain/` e **ler as regras de negócio inteiras**, sem um `import` de framework.
