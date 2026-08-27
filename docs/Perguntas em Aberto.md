---
tags: [duvida, backlog]
---

# Perguntas em Aberto

Decisões de negócio que o código **não** responde e que ninguém registrou. Cada uma
vira uma [[Template - ADR|ADR]] quando for respondida. Enquanto estiverem aqui, qualquer
implementação que dependa delas precisa de confirmação antes.

## Preço e conformidade
- [ ] **Revenda acima do valor original é permitida em todo tipo de evento?** Hoje é livre
      ([[RN-004 Preço de revenda é livre]]). Existe legislação específica para eventos
      esportivos no Brasil — precisa de validação jurídica antes de virar produto.
- [ ] Existe teto de preço, ou alerta visual quando `price > originalPrice`?
- [ ] O `originalPrice` é declarado pelo vendedor sem prova. Vale exigir comprovante?

## Transação
- [ ] **Como o comprador e o vendedor se encontram?** Hoje não há mensagem, telefone exposto
      nem pedido — ver [[RN-014 Plataforma não intermedia pagamento]].
- [ ] Haverá entidade `Order`/`Purchase`? Isso muda o [[Status do Anúncio]] (entra `RESERVADO`).
- [ ] Quem confirma que a venda ocorreu: vendedor, comprador, ou ambos?
- [ ] Modelo de receita: taxa por venda, assinatura, destaque pago? Nada disso está modelado.

## Confiança e fraude
- [ ] Como impedir venda do mesmo ingresso duas vezes? Não há vínculo com ingresso real.
- [ ] Verificação de identidade do vendedor (CPF, e-mail confirmado)? Hoje o cadastro é aberto.
- [ ] Avaliação de vendedor / reputação — citado no `README` como próximo passo, não modelado.

## Catálogo
- [ ] **Quem cria evento?** Hoje qualquer usuário logado — ver [[RN-010 Quem pode cadastrar evento]].
      Precisa de papel `ADMIN`, curadoria, ou integração com fonte oficial?
- [ ] Eventos duplicados: qual a chave natural (nome + data + local)?
- [ ] Evento que já passou some da vitrine? Não há regra hoje.

## Ciclo de vida
- [ ] Anúncio expira sozinho quando o evento acontece?
- [ ] Vendedor pode reativar um anúncio cancelado? Ver [[RN-007 Ciclo de vida do anúncio]].
- [ ] Vendedor pode editar preço de um anúncio ativo? Não há endpoint de edição.
