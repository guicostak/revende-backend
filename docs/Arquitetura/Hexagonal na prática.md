---
tags: [arquitetura, referencia, hexagonal]
---

# Hexagonal na prática

Síntese do que praticantes relatam ao aplicar ports & adapters em Spring Boot — o que
dá errado de verdade, não a teoria. Contrato normativo em [[CLAUDE]] §1.

## As quatro armadilhas mais relatadas

### 1. Over-engineering em projeto pequeno

A queixa mais comum, e a mais honesta: hexagonal tem sobrecusto e foi feita para
**domínio complexo**, não para CRUD com cinco endpoints. Aplicada em projeto simples,
cada tarefa trivial vira três arquivos.

**No Revende isso se aplica de forma desigual.** O [[Contexto Marketplace]] tem regra real
— posse, transição de estado, preço — e paga o custo. O [[Contexto Catalog]] é quase CRUD
puro: evento tem nome, data e local, e ninguém valida nada. Aplicar a estrutura completa
nos dois é cerimônia sem retorno no segundo.

> Recomendação: comece organizando por funcionalidade dentro do módulo de aplicação e
> **evolua para a separação completa quando a regra aparecer**. Estrutura não cria domínio.

### 2. Ports mal definidos

Port genérico demais (`Repository<T>`) devolve a abstração vazada que ele deveria esconder.
Port deve nascer da **necessidade do caso de uso**, com o nome do que o domínio precisa:
`LoadListingPort`, não `ListingRepository`.

### 3. Anotação de framework vazando para o domínio

`@Transactional`, `@Autowired` e `@Component` dentro do domínio acoplam ao Spring e matam
o motivo de existir da arquitetura. É o que o teste ArchUnit precisa impedir mecanicamente
— revisão humana não pega isso de forma confiável.

### 4. Explosão de mappers

Cada fronteira quer um mapper: domínio↔JPA, domínio↔DTO. Em domínio grande vira mais
código de tradução que de negócio. Mitigações: MapStruct para gerar o boilerplate, ou
aceitar o mapeamento manual enquanto o modelo é pequeno — que é o caso aqui.

## O que fica

Hexagonal compensa quando **a regra de negócio é o ativo** e você quer lê-la sem ruído.
Se o valor está em outro lugar — integração, volume, latência — o custo pode não pagar.

## Fontes

- [Hexagonal Architecture with Spring Boot — Arho Huttunen](https://www.arhohuttunen.com/hexagonal-architecture-spring-boot/)
- [Hexagonal Architecture Best Practices for Spring Boot Developers](https://medium.com/but-it-works-on-my-machine/hexagonal-architecture-best-practices-for-spring-boot-developers-6dd2a60602c3)
- [Hexagonal Architecture in Spring Boot: A Practical Guide — DEV](https://dev.to/jhonifaber/hexagonal-architecture-or-port-adapters-23ed)
