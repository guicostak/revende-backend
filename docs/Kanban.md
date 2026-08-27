---

kanban-plugin: board
tags: [kanban, plano]
issues: https://github.com/guicostak/revende-backend/issues

---

## 🔒 Bloqueado — falta decisão

- [ ] **D-01** Quem pode cadastrar evento? — [[RN-010 Quem pode cadastrar evento]] · [#21](https://github.com/guicostak/revende-backend/issues/21)
- [ ] **D-02** A plataforma vai intermediar a transação? — [[RN-014 Plataforma não intermedia pagamento]] · [#22](https://github.com/guicostak/revende-backend/issues/22)
- [ ] **D-03** Anúncio não-ativo acessado por ID: 404 ou 200 com status? — [[UC-07 Detalhar anúncio]] · [#23](https://github.com/guicostak/revende-backend/issues/23)
- [ ] **D-04** `?city=` + `?name=` juntos: combinar com AND ou recusar? — [[RN-013 Busca de eventos por cidade ou nome]] · [#24](https://github.com/guicostak/revende-backend/issues/24)
- [ ] **D-05** Ágio tem teto ou sinalização? (depende de jurídico) — [[RN-004 Preço de revenda é livre]] · [#25](https://github.com/guicostak/revende-backend/issues/25)
- [ ] **D-06** Como comprador e vendedor se encontram? — [[Perguntas em Aberto]] · [#26](https://github.com/guicostak/revende-backend/issues/26)

## 🎯 Sprint 1 — rede de segurança

- [ ] **T-01** ArchUnit + testes de caracterização dos endpoints — [[Planejamento de Tasks#T-01]] · [#1](https://github.com/guicostak/revende-backend/issues/1)
- [ ] **T-02** Login com senha errada devolve 401, não 500 — [[UC-02 Autenticar]] · [#2](https://github.com/guicostak/revende-backend/issues/2)
- [ ] **T-03** Falta de posse devolve 403, não 400 — [[RN-006 Apenas o dono altera o anúncio]] · [#3](https://github.com/guicostak/revende-backend/issues/3)
- [ ] **T-04** Transição de status inválida devolve 409 — [[RN-007 Ciclo de vida do anúncio]] · [#4](https://github.com/guicostak/revende-backend/issues/4)
- [ ] **T-05** `/api/listings/me` sem token devolve 401, não 500 — [[UC-08 Listar meus anúncios]] · [#5](https://github.com/guicostak/revende-backend/issues/5)

## 📥 Backlog — segurança e infraestrutura

- [ ] **T-06** Recusar boot fora de dev com o segredo JWT padrão — [[RN-003 Sessão stateless por JWT]] · [#6](https://github.com/guicostak/revende-backend/issues/6)
- [ ] **T-07** Logar falha de autenticação no `JwtAuthFilter` — [[RN-003 Sessão stateless por JWT]] · [#7](https://github.com/guicostak/revende-backend/issues/7)
- [ ] **T-08** Flyway + `ddl-auto: validate` — [[Dívidas Técnicas]] · [#8](https://github.com/guicostak/revende-backend/issues/8)
- [ ] **T-09** `ErrorResponse` tipado no lugar de `Map<String,Object>` — [[Qualidade de Código]] · [#9](https://github.com/guicostak/revende-backend/issues/9)

## 📥 Backlog — API e performance

- [ ] **T-10** `LAZY` + `JOIN FETCH` na vitrine (mata o N+1) — [[UC-06 Listar anúncios ativos]] · [#10](https://github.com/guicostak/revende-backend/issues/10)
- [ ] **T-11** Paginação em `/api/events` e `/api/listings` — [[RN-013 Busca de eventos por cidade ou nome]] · [#11](https://github.com/guicostak/revende-backend/issues/11)
- [ ] **T-12** `@Transactional` nos casos de uso de escrita — [[UC-09 Publicar anúncio]] · [#12](https://github.com/guicostak/revende-backend/issues/12)
- [ ] **T-13** `ticketType` inválido devolve 400 com os valores aceitos — [[UC-09 Publicar anúncio]] · [#13](https://github.com/guicostak/revende-backend/issues/13)

## 📥 Backlog — migração hexagonal

- [ ] **T-14** Domínio puro: `TicketListing` com invariantes — [[Mapa de Migração]] · [#14](https://github.com/guicostak/revende-backend/issues/14)
- [ ] **T-15** `TicketListingJpaEntity` + mapper + adapter de persistência — [[Mapa de Migração]] · [#15](https://github.com/guicostak/revende-backend/issues/15)
- [ ] **T-16** Ports de saída do Marketplace — [[Arquitetura Hexagonal]] · [#16](https://github.com/guicostak/revende-backend/issues/16)
- [ ] **T-17** Um service por caso de uso + ports de entrada — [[Arquitetura Hexagonal]] · [#17](https://github.com/guicostak/revende-backend/issues/17)
- [ ] **T-18** Controller passa a depender das interfaces — [[Mapa de Migração]] · [#18](https://github.com/guicostak/revende-backend/issues/18)
- [ ] **T-19** Migrar [[Contexto Catalog]] — inclui `LoadEventPort` · [#19](https://github.com/guicostak/revende-backend/issues/19)
- [ ] **T-20** Migrar [[Contexto Identity]] — por último, mexe em segurança · [#20](https://github.com/guicostak/revende-backend/issues/20)

## 🚧 Em andamento

## 👀 Em revisão

## ✅ Concluído

**Complete**

- [x] **T-00a** Remover dados de demonstração (`DataSeeder`) e console H2
- [x] **T-00b** Segredo JWT para variável de ambiente — [[RN-003 Sessão stateless por JWT]]
- [x] **T-00c** Vault de domínio + contrato de arquitetura ([[CLAUDE]])

%% kanban:settings
```
{"kanban-plugin":"board","list-collapse":[false,false,false,false,false,false,false,false],"show-checkboxes":true,"new-note-folder":"docs"}
```
%%
