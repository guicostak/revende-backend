---
tags: [moc, adr]
---

# Índice de Decisões (ADR)

Decisão estrutural registrada é decisão que não precisa ser rediscutida a cada sprint.
Uma ADR **nunca é editada depois de aceita** — é substituída por outra que a supera.

| # | Decisão | Status | Data |
|---|---|---|---|
| 0001 | [[ADR-0001 Adotar arquitetura hexagonal]] | Aceita | 2026-08-27 |
| 0002 | [[ADR-0002 JWT stateless sem refresh token]] | Aceita (com ressalva) | 2026-08-27 |
| 0003 | [[ADR-0003 Cancelamento lógico de anúncio]] | Aceita | 2026-08-27 |

## Decisões que faltam

Cada uma bloqueia trabalho real — ver [[Perguntas em Aberto]]:

- Quem pode cadastrar evento ([[RN-010 Quem pode cadastrar evento]])
- Se a plataforma vai intermediar a transação ([[RN-014 Plataforma não intermedia pagamento]])
- Teto ou sinalização de ágio ([[RN-004 Preço de revenda é livre]])
- Visibilidade de anúncio não-ativo por ID ([[UC-07 Detalhar anúncio]])

Nova decisão → [[Template - ADR]]. Voltar: [[Home]]
