---
tags: [arquitetura, qualidade]
fonte-da-verdade: CLAUDE.md
---

# Qualidade de Código

> [!important] Regras normativas em [[CLAUDE]] §2 e §3
> Esta nota é o **porquê**; o checklist executável está no [[CLAUDE]].

## O que não se negocia

| Regra | Motivo no domínio do Revende |
|---|---|
| `BigDecimal` para dinheiro | `price` e `originalPrice` são dinheiro real de terceiros — `double` erra centavo |
| Toda lista paginada | A vitrine ([[UC-06 Listar anúncios ativos]]) é a tela mais acessada e cresce sem teto |
| `LAZY` + `JOIN FETCH` | `EAGER` em `event` e `seller` já garante N+1 hoje |
| Status HTTP correto | 400 no lugar de 403 esconde tentativa de acesso indevido — ver [[RN-006 Apenas o dono altera o anúncio]] |
| Segredo só em env var | O segredo JWT versionado permite forjar token de qualquer conta |
| Sem `catch (Exception ignored)` | Em `JwtAuthFilter`, engole falha de autenticação sem rastro |

## Testes

Nenhuma regra deste vault está protegida por teste — `src/test/java` está vazio.
Enquanto isso, **toda nota marcada `#implementada` é uma promessa sem garantia**.

Prioridade de cobertura, por risco:

1. [[RN-006 Apenas o dono altera o anúncio]] — falha = sabotagem entre usuários
2. [[RN-007 Ciclo de vida do anúncio]] — falha = venda desfeita, histórico destruído
3. [[RN-001 E-mail único por conta]] e [[RN-003 Sessão stateless por JWT]] — falha = conta invadida
4. [[RN-008 Vitrine mostra apenas anúncios ativos]] — falha = oferta morta na vitrine

O nome do teste é a regra em inglês: `shouldRejectMarkSoldWhenRequesterIsNotSeller()`.

## Definição de pronto

O checklist completo está em [[CLAUDE]] §3. Um item extra, específico deste vault:

- [ ] **A nota da regra afetada foi atualizada no mesmo commit.**

Ver [[Dívidas Técnicas]] para o passivo atual.
