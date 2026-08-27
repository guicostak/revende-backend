---
tags: [adr, arquitetura]
status: aceita
data: 2026-08-27
---

# ADR-0001 — Adotar arquitetura hexagonal

**Status:** Aceita · **Data:** 2026-08-27

## Contexto

A aplicação nasceu em camadas clássicas (`controller → service → repository`), com as
entidades JPA servindo de modelo de domínio. Consequências observadas no código atual:

- regra de negócio espalhada entre `ListingService`, anotações de DTO e `SecurityConfig`;
- entidades anêmicas: `setStatus` público permite qualquer transição
  ([[RN-007 Ciclo de vida do anúncio]] não é validada por ninguém);
- nada é testável sem Spring — e o projeto tem **zero testes**;
- `ListingService` chama `EventService` direto, colando dois contextos.

## Decisão

Adotar **ports & adapters**, com domínio livre de framework, ports de entrada por caso de uso
e ports de saída para toda dependência externa. O contrato normativo é o [[CLAUDE]] §1,
lido antes de qualquer task. A migração é incremental, por contexto — ver [[Mapa de Migração]].

## Consequências

**Positivas:** regra de negócio em um lugar só e testável em milissegundos; troca de
persistência sem tocar no domínio; fronteira entre contextos explícita.

**Negativas:** mais classes e um mapper por agregado; curva de aprendizado; risco de
"hexagonal cerimonial" em CRUD puro.

**Mitigação:** ArchUnit no build para impedir a erosão silenciosa ([[CLAUDE]] §1.6).
Sem esse teste, a decisão vira parágrafo bonito e o código volta ao que era.

## Alternativas descartadas

- **Manter camadas e só melhorar testes** — não resolve a anemia nem o vazamento entre contextos.
- **Clean Architecture completa** — mais cerimônia sem ganho perceptível nesta escala.
