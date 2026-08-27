---

kanban-plugin: board
tags: [kanban, plano]

---

## 🔒 Bloqueado — falta decisão

- [ ] **D-01** Quem pode cadastrar evento? — [[RN-010 Quem pode cadastrar evento]]
- [ ] **D-02** A plataforma vai intermediar a transação? — [[RN-014 Plataforma não intermedia pagamento]]
- [ ] **D-03** Anúncio não-ativo acessado por ID: 404 ou 200 com status? — [[UC-07 Detalhar anúncio]]
- [ ] **D-04** `?city=` + `?name=` juntos: combinar com AND ou recusar? — [[RN-013 Busca de eventos por cidade ou nome]]
- [ ] **D-05** Ágio tem teto ou sinalização? (depende de jurídico) — [[RN-004 Preço de revenda é livre]]
- [ ] **D-06** Como comprador e vendedor se encontram? — [[Perguntas em Aberto]]

## 🎯 Sprint 1 — rede de segurança

- [ ] **T-01** ArchUnit + testes de caracterização dos endpoints — [[Planejamento de Tasks#T-01]]
- [ ] **T-02** Login com senha errada devolve 401, não 500 — [[UC-02 Autenticar]]
- [ ] **T-03** Falta de posse devolve 403, não 400 — [[RN-006 Apenas o dono altera o anúncio]]
- [ ] **T-04** Transição de status inválida devolve 409 — [[RN-007 Ciclo de vida do anúncio]]
- [ ] **T-05** `/api/listings/me` sem token devolve 401, não 500 — [[UC-08 Listar meus anúncios]]

## 📥 Backlog — segurança e infraestrutura

- [ ] **T-06** Recusar boot fora de dev com o segredo JWT padrão — [[RN-003 Sessão stateless por JWT]]
- [ ] **T-07** Logar falha de autenticação no `JwtAuthFilter` — [[RN-003 Sessão stateless por JWT]]
- [ ] **T-08** Flyway + `ddl-auto: validate` — [[Dívidas Técnicas]]
- [ ] **T-09** `ErrorResponse` tipado no lugar de `Map<String,Object>` — [[Qualidade de Código]]

## 📥 Backlog — API e performance

- [ ] **T-10** `LAZY` + `JOIN FETCH` na vitrine (mata o N+1) — [[UC-06 Listar anúncios ativos]]
- [ ] **T-11** Paginação em `/api/events` e `/api/listings` — [[RN-013 Busca de eventos por cidade ou nome]]
- [ ] **T-12** `@Transactional` nos casos de uso de escrita — [[UC-09 Publicar anúncio]]
- [ ] **T-13** `ticketType` inválido devolve 400 com os valores aceitos — [[UC-09 Publicar anúncio]]

## 📥 Backlog — migração hexagonal

- [ ] **T-14** Domínio puro: `TicketListing` com invariantes — [[Mapa de Migração]]
- [ ] **T-15** `TicketListingJpaEntity` + mapper + adapter de persistência — [[Mapa de Migração]]
- [ ] **T-16** Ports de saída do Marketplace — [[Arquitetura Hexagonal]]
- [ ] **T-17** Um service por caso de uso + ports de entrada — [[Arquitetura Hexagonal]]
- [ ] **T-18** Controller passa a depender das interfaces — [[Mapa de Migração]]
- [ ] **T-19** Migrar [[Contexto Catalog]] — inclui `LoadEventPort`
- [ ] **T-20** Migrar [[Contexto Identity]] — por último, mexe em segurança

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
