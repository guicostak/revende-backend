---
tags: [negocio, referencia]
figma: https://www.figma.com/board/19xFLEgRJ48vOcB9eHk8Gi
---

# Business Model Canvas

🔗 **Board FigJam:** https://www.figma.com/board/19xFLEgRJ48vOcB9eHk8Gi

Os 9 blocos do modelo de negócio, preenchidos a partir do que o **código realmente faz** —
não do que a gente gostaria que ele fizesse. Cada afirmação do board tem lastro numa nota
deste vault.

## Legenda do board

| Marcador | Significado |
|---|---|
| ✅ | Já existe no produto |
| ⚠️ | Existe pela metade |
| ❌ | Não existe e trava o modelo |

## O que o canvas revelou

1. **Marketplace de dois lados com um lado só.** O comprador não existe como entidade —
   ver [[RN-014 Plataforma não intermedia pagamento]] e [[Glossário]].
2. **Receita zero, e não por falta de implementação: por falta de decisão.** Comissão exige
   intermediar o pagamento; sem isso, `VENDIDO` é declaração unilateral sem prova
   ([[UC-10 Marcar anúncio como vendido]]) e não há fato auditável para cobrar em cima.
3. **O único diferencial real entregue hoje** é a transparência do ágio: preço original e
   preço pedido lado a lado ([[RN-004 Preço de revenda é livre]]).
4. **Confiança é o buraco central.** Sem reputação, sem verificação de identidade, sem
   mediação — estranhos fechando negócio fora da plataforma.

## As 6 decisões do board

| # | Decisão | Nota |
|---|---|---|
| D-01 | Quem cadastra evento | [[RN-010 Quem pode cadastrar evento]] |
| D-02 | Plataforma intermedia a transação? | [[RN-014 Plataforma não intermedia pagamento]] |
| D-03 | Anúncio não-ativo por link direto | [[UC-07 Detalhar anúncio]] |
| D-04 | Busca por cidade e nome juntos | [[RN-013 Busca de eventos por cidade ou nome]] |
| D-05 | Teto ou aviso de ágio | [[RN-004 Preço de revenda é livre]] |
| D-06 | Contato comprador ↔ vendedor | [[Perguntas em Aberto]] |

As mesmas seis estão na coluna **🔒 Bloqueado** do [[Kanban]]. Resposta dada no board →
vira [[Template - ADR|ADR]] em [[Índice de Decisões]] → destrava as tasks em
[[Planejamento de Tasks]].
