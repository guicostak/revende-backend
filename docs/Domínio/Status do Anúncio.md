---
tags: [dominio, enum, marketplace]
contexto: Marketplace
codigo: src/main/java/com/revende/backend/model/ListingStatus.java
---

# Status do Anúncio

`ListingStatus` — situação de um [[Anúncio de Ingresso]]. Persistido como `STRING`
(`@Enumerated(EnumType.STRING)`), então **renomear um valor quebra os dados existentes**.

| Valor | Significado | Visível na vitrine | Terminal |
|---|---|---|---|
| `ATIVO` | Oferta valendo, aberta a interessados | ✅ sim | não |
| `VENDIDO` | Vendedor declarou a venda concluída | ❌ não | sim |
| `CANCELADO` | Vendedor desistiu da oferta | ❌ não | sim |

Transições em [[RN-007 Ciclo de vida do anúncio]]. Filtro da vitrine em
[[RN-008 Vitrine mostra apenas anúncios ativos]].

> [!note] Estado que vai faltar
> Se a plataforma passar a intermediar a compra ([[RN-014 Plataforma não intermedia pagamento]]),
> entra um `RESERVADO` entre `ATIVO` e `VENDIDO`, e `VENDIDO` deixa de ser declarado
> unilateralmente pelo vendedor. Isso é uma mudança de domínio, não de código: exige ADR.
