---
tags: [moc, regra-de-negocio]
---

# Índice de Regras

Cada regra tem um enunciado em linguagem de negócio, a razão de existir, onde está no código
e o que falta. **Antes de alterar comportamento, leia a regra afetada; depois de alterar,
atualize a nota no mesmo commit.**

| # | Regra | Contexto | Status |
|---|---|---|---|
| RN-001 | [[RN-001 E-mail único por conta]] | Identity | `#implementada` |
| RN-002 | [[RN-002 Senha mínima e hash BCrypt]] | Identity | `#implementada` |
| RN-003 | [[RN-003 Sessão stateless por JWT]] | Identity | `#parcial` |
| RN-004 | [[RN-004 Preço de revenda é livre]] | Marketplace | `#implementada` ⚠️ jurídico |
| RN-005 | [[RN-005 Anúncio nasce ativo]] | Marketplace | `#implementada` |
| RN-006 | [[RN-006 Apenas o dono altera o anúncio]] | Marketplace | `#parcial` (HTTP 400 em vez de 403) |
| RN-007 | [[RN-007 Ciclo de vida do anúncio]] | Marketplace | `#ausente` (transição não validada) |
| RN-008 | [[RN-008 Vitrine mostra apenas anúncios ativos]] | Marketplace | `#parcial` (furo em `/listings/{id}`) |
| RN-009 | [[RN-009 Lote de ingressos é indivisível]] | Marketplace | `#implementada` |
| RN-010 | [[RN-010 Quem pode cadastrar evento]] | Catalog | `#parcial` ⚠️ sem papel |
| RN-011 | [[RN-011 Anúncio exige evento existente]] | Marketplace | `#implementada` |
| RN-012 | [[RN-012 Cancelamento é lógico]] | Marketplace | `#implementada` |
| RN-013 | [[RN-013 Busca de eventos por cidade ou nome]] | Catalog | `#implementada` |
| RN-014 | [[RN-014 Plataforma não intermedia pagamento]] | Marketplace | `#implementada` (por omissão) |

Voltar: [[Home]] · Ver também [[Índice de Casos de Uso]] · [[Dívidas Técnicas]]
