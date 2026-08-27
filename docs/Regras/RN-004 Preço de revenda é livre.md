---
tags: [regra-de-negocio, marketplace, implementada, atencao]
contexto: Marketplace
codigo: src/main/java/com/revende/backend/dto/ListingDtos.java
---

# RN-004 — Preço de revenda é livre

> O vendedor define o preço de revenda sem teto. Ele pode ser **maior ou menor** que o
> preço original. Ambos precisam ser positivos.

## Por quê

Modelo de marketplace aberto: o preço é do vendedor, e o `originalPrice` serve de
referência para o comprador avaliar o ágio ou o desconto.

## Implementação

`@NotNull @Positive` em `originalPrice` e `price` no `ListingRequest`. Nenhuma comparação
entre os dois. Ambos são `BigDecimal` — correto para dinheiro ([[Qualidade de Código]]).


## ⚠️ Risco em aberto

Revenda com ágio é **assunto regulado no Brasil** e as regras variam por tipo de evento —
espetáculos esportivos têm tratamento próprio. O código hoje não distingue nada disso.

**Nenhuma feature de destaque, sugestão de preço ou cobrança sobre o ágio deve ser
implementada antes de resposta jurídica.** Ver [[Perguntas em Aberto]].

## Furos

- `originalPrice` é declarado pelo vendedor, sem comprovante — pode ser inflado para
  simular desconto.
- Sem moeda: assume-se BRL implicitamente. Um VO `Money` resolveria.
- Sem teto, alerta ou sinalização visual de ágio.

## Testes esperados

- preço zero ou negativo → 400
- preço acima do original → aceito (comportamento atual, explicitamente testado)
- preço abaixo do original → aceito

Aplicada em [[UC-09 Publicar anúncio]] · afeta [[Anúncio de Ingresso]].
