---
tags: [adr, marketplace]
status: aceita
data: 2026-08-27
---

# ADR-0003 — Cancelamento lógico de anúncio

**Status:** Aceita · **Data:** 2026-08-27

## Contexto

`DELETE /api/listings/{id}` precisa tirar a oferta da vitrine. A implementação óbvia seria
remover a linha do banco.

## Decisão

**Nada é apagado.** O cancelamento muda a situação para `CANCELADO` e o registro permanece.
Ver [[RN-012 Cancelamento é lógico]] e [[Status do Anúncio]].

## Justificativa

O histórico é ativo do marketplace: base para reputação futura, resolução de disputa e
detecção de padrão de fraude (publicar e cancelar em série). Apagar destrói prova que não
volta. A perda de espaço é irrelevante na escala do produto.

## Consequências

**Positivas:** histórico completo do vendedor em [[UC-08 Listar meus anúncios]]; auditoria possível.

**Negativas:**
- o verbo `DELETE` não faz o que o nome diz — mitigado no [[Glossário]] (termos proibidos);
- a tabela só cresce;
- **LGPD**: pedido de exclusão de conta exige *anonimizar* o anúncio, não apagá-lo.
  Não há tratamento disso hoje — ver [[Perguntas em Aberto]].

## Alternativa descartada

**Exclusão física** — simples, e destrói o histórico de forma irreversível.
