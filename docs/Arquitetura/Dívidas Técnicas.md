---
tags: [arquitetura, divida]
---

# Dívidas Técnicas

Passivo levantado na leitura do código. A lista canônica e numerada está em [[CLAUDE]] §4 —
aqui cada item ganha **impacto de negócio** e link para a regra afetada.

| # | Dívida | Impacto | Regra afetada |
|---|---|---|---|
| 1 | Zero testes | Toda regra deste vault é promessa sem garantia | todas |
| 2 | Produção depende de `REVENDE_JWT_SECRET` estar setado | Se a env var faltar, o default de dev vale em produção e qualquer um forja token | [[RN-003 Sessão stateless por JWT]] |
| 3 | `ddl-auto: update`, sem Flyway | Schema não versionado; migração para prod é imprevisível | — |
| 4 | Falta de posse responde 400 | Esconde tentativa de acesso indevido; front não distingue | [[RN-006 Apenas o dono altera o anúncio]] |
| 5 | `catch (Exception ignored)` no filtro JWT | Falha de autenticação some sem log | [[RN-003 Sessão stateless por JWT]] |
| 6 | `FetchType.EAGER` em `TicketListing` | N+1 na vitrine, a tela mais acessada | [[RN-008 Vitrine mostra apenas anúncios ativos]] |
| 7 | Sem `@Transactional` | Escrita sem fronteira transacional explícita | [[UC-09 Publicar anúncio]] |
| 8 | Sem paginação | Vitrine e catálogo devolvem tudo | [[RN-008 Vitrine mostra apenas anúncios ativos]], [[RN-013 Busca de eventos por cidade ou nome]] |
| 9 | Sem papel/role | Qualquer conta cria evento, sem autoria | [[RN-010 Quem pode cadastrar evento]] |
| 10 | Entidades anêmicas com setters públicos | Invariante nenhuma protegida | [[Anúncio de Ingresso]] |

## Fora da lista do CLAUDE (achados de domínio)

| Achado | Impacto | Onde |
|---|---|---|
| Transição de status não validada | Venda pode ser desfeita; histórico destruído | [[RN-007 Ciclo de vida do anúncio]] |
| Login com senha errada → 500 | Erro mais comum da app devolve erro de servidor | [[UC-02 Autenticar]] |
| `/api/listings/me` sem token → 500 | NPE em vez de 401 | [[UC-08 Listar meus anúncios]] |
| `city` + `name` juntos ignora `name` | Resultado silenciosamente errado | [[RN-013 Busca de eventos por cidade ou nome]] |
| Sem canal de contato comprador↔vendedor | Produto não fecha o ciclo | [[RN-014 Plataforma não intermedia pagamento]] |

## Já resolvido

- ~~Console H2 liberado sem perfil~~ — removido junto com os dados de demonstração
- ~~`DataSeeder` em qualquer perfil~~ — arquivo excluído; o banco sobe vazio
