# Revende Backend — Arquitetura & Qualidade

> **Leia este arquivo antes de executar qualquer task.** Ele define a arquitetura-alvo
> (hexagonal / ports & adapters) e o padrão de qualidade obrigatório.
> Se uma task pedir algo que conflite com este documento, **avise antes de implementar**.

Stack: Java 21 · Spring Boot 3.3 · Maven · Spring Security (JWT) · Spring Data JPA · H2 (dev) / PostgreSQL (prod).
Domínio: marketplace de revenda de ingressos (usuários, eventos, anúncios de ingresso).

---

## 0. Base de conhecimento (Obsidian)

Este arquivo é o **contrato normativo**. O *porquê* de cada regra — o domínio, as regras de
negócio e os casos de uso — vive no vault Obsidian em [`docs/`](docs/Home.md).
Abra a **raiz do projeto** como vault (a config já está em `.obsidian/`).

| Preciso de… | Vá para |
|---|---|
| Visão geral e mapa | [docs/Home.md](docs/Home.md) · `docs/Revende.canvas` |
| Vocabulário do projeto | [docs/Glossário.md](docs/Glossário.md) |
| Regras de negócio (RN-001…014) | [docs/Regras/](docs/Regras/Índice%20de%20Regras.md) |
| Casos de uso (UC-01…11) | [docs/Casos de Uso/](docs/Casos%20de%20Uso/Índice%20de%20Casos%20de%20Uso.md) |
| Decisões estruturais (ADRs) | [docs/Decisões/](docs/Decisões/Índice%20de%20Decisões.md) |
| O que não foi decidido | [docs/Perguntas em Aberto.md](docs/Perguntas%20em%20Aberto.md) |

> [!important] Antes de alterar comportamento
> 1. Leia a **regra de negócio** afetada em `docs/Regras/` e o **caso de uso** em `docs/Casos de Uso/`.
> 2. Se a task contradiz uma regra documentada, **avise antes de implementar** — pode ser
>    mudança de negócio disfarçada de bug.
> 3. Se a alteração muda o comportamento descrito, **atualize a nota no mesmo commit**.
>    Documentação desatualizada mente com autoridade.
> 4. Regra nova → `docs/Templates/`. Decisão estrutural → ADR.

---

## 1. Arquitetura Hexagonal (Ports & Adapters)

### 1.1 A regra única

**A dependência sempre aponta para dentro.** O domínio não conhece ninguém; a aplicação
conhece o domínio; os adapters conhecem a aplicação. Nunca o contrário.

```
        HTTP / CLI / Scheduler                   JPA / SMTP / HTTP client
                 │                                          │
        ┌────────▼─────────┐                     ┌──────────▼─────────┐
        │  adapter/in/web  │                     │ adapter/out/persist│
        └────────┬─────────┘                     └──────────▲─────────┘
                 │ chama port/in                  implementa │ port/out
        ┌────────▼──────────────────────────────────────────┴─────────┐
        │                       application                            │
        │            (use cases: orquestra, transaciona)               │
        └────────────────────────────┬─────────────────────────────────┘
                                     │ usa
                        ┌────────────▼────────────┐
                        │         domain          │
                        │  entidades · VOs · regras│
                        │   ZERO framework         │
                        └─────────────────────────┘
```

### 1.2 Estrutura de pacotes alvo

```
com.revende.backend
├── shared/                          # kernel comum, sem regra de negócio
│   ├── domain/                      # Money, EmailAddress, exceções base
│   └── config/                      # SecurityConfig, CorsConfig, beans Spring
│
├── identity/                        # contexto: usuário & autenticação
├── catalog/                         # contexto: eventos
└── marketplace/                     # contexto: anúncios de ingresso
     │
     ├── domain/
     │   ├── model/                  # TicketListing, ListingStatus, Money — POJOs puros
     │   └── exception/              # ListingNotFoundException, NotListingOwnerException
     │
     ├── application/
     │   ├── port/in/                # PublishListingUseCase, MarkListingSoldUseCase (interfaces)
     │   ├── port/out/               # LoadListingPort, SaveListingPort (interfaces)
     │   └── service/                # PublishListingService implements PublishListingUseCase
     │
     └── adapter/
         ├── in/web/                 # ListingController + DTOs + WebMapper
         └── out/persistence/        # TicketListingJpaEntity, SpringDataRepo, PersistenceAdapter
```

### 1.3 O que pode existir em cada camada

| Camada | Pode | **Não pode**, nunca |
|---|---|---|
| `domain/` | Java puro, regras de negócio, invariantes, VOs imutáveis | `org.springframework.*`, `jakarta.persistence.*`, `jakarta.validation.*`, Jackson, Lombok mágico |
| `application/` | `@Service`, `@Transactional`, ports, orquestração | Tipos HTTP (`ResponseEntity`, `Authentication`), entidades JPA, SQL |
| `adapter/in/web` | `@RestController`, DTOs, validação Bean, mapeamento HTTP↔domínio | Regra de negócio, acesso a repositório |
| `adapter/out/persistence` | `@Entity` JPA, Spring Data, mapeamento domínio↔tabela | Regra de negócio, decisão de fluxo |

### 1.4 Regras não-negociáveis

1. **Entidade de domínio ≠ entidade JPA.** São duas classes diferentes, ligadas por um mapper
   no adapter de persistência. `TicketListing` (domínio) vs `TicketListingJpaEntity` (adapter).
2. **Toda saída do domínio passa por um port** (`interface` em `application/port/out`),
   implementado no adapter. O `application` nunca importa `JpaRepository`.
3. **Todo caso de uso é um port de entrada** (`application/port/in`) com um método público.
   O controller depende da interface, nunca da classe concreta.
4. **DTO de web nunca entra no domínio e entidade de domínio nunca sai pela web.**
   O mapeamento acontece na borda.
5. **Regra de negócio mora no domínio**, não no service. Ex.: "só o dono cancela o anúncio"
   é `listing.cancelBy(sellerId)` — não um `if` no service.
6. **Identidade do usuário chega como tipo próprio** (`SellerId` / `UserId`), não como
   `String email` vindo de `Authentication.getName()`.
7. Um contexto (`marketplace`) só fala com outro (`catalog`) através de um **port de saída**,
   nunca importando o service do outro.

### 1.5 Mapa: código atual → destino

| Hoje | Vai para |
|---|---|
| `controller/ListingController` | `marketplace/adapter/in/web/ListingController` |
| `dto/ListingDtos` | `marketplace/adapter/in/web/dto/` (um record por arquivo) |
| `service/ListingService` | quebrar em `application/service/*Service`, um por caso de uso |
| `model/TicketListing` (@Entity) | `domain/model/TicketListing` (puro) + `adapter/out/persistence/TicketListingJpaEntity` |
| `repository/TicketListingRepository` | `adapter/out/persistence/` + ports em `application/port/out/` |
| `security/`, `config/SecurityConfig` | `shared/config/` e `identity/adapter/in/web/security/` |
| `exception/GlobalExceptionHandler` | `shared/adapter/in/web/` |

**Migração é incremental.** Não reescreva tudo de uma vez: mova um contexto por task,
começando por `marketplace`. Código novo já nasce na estrutura nova.

### 1.6 Como proteger a arquitetura

Adicionar ArchUnit (`com.tngtech.archunit:archunit-junit5`, escopo `test`) com testes que
falham o build se: `domain` importar `org.springframework` ou `jakarta.persistence`;
`application` importar `adapter`; qualquer ciclo entre pacotes. **Esse teste é o guardião —
não desabilite para "passar" uma task.**

---

## 2. Padrões de código

### 2.1 Geral

- Java 21: use `record` para DTOs e VOs, `sealed` para hierarquias fechadas, pattern matching.
- **Injeção por construtor**, sempre. Nunca `@Autowired` em campo.
- Campos `final` por padrão. Objetos de domínio imutáveis sempre que possível.
- Nada de `null` em API pública: use `Optional` no retorno de busca, ou lance exceção.
- `BigDecimal` para dinheiro — nunca `double`/`float`. Comparar com `compareTo`, não `equals`.
- Um tipo público por arquivo. Nada de classes-container tipo `ListingDtos` com records dentro.
- Sem números/strings mágicos: constante nomeada ou config.
- Idioma: **código, tipos e nomes em inglês**; mensagens de erro ao usuário em português.

### 2.2 Camada web

- Controller é fino: recebe, valida (`@Valid`), delega ao use case, mapeia a resposta. Sem `if` de negócio.
- Status corretos: `201` + `Location` ao criar, `204` ao deletar, `400` validação,
  `401` não autenticado, `403` sem permissão, `404` inexistente, `409` conflito de estado.
- **Toda lista pública é paginada** (`Pageable` + `Page<T>`). Nunca devolver `findAll()` cru.
- Erros seguem um contrato único e tipado (`ErrorResponse` record), não `Map<String,Object>`.

### 2.3 Persistência

- Relações `@ManyToOne` são **`FetchType.LAZY`**. Consultas de listagem usam `JOIN FETCH`
  ou projeção — N+1 é bug, não detalhe.
- `@Transactional` nos use cases de escrita; `@Transactional(readOnly = true)` nos de leitura.
- **Schema versionado com Flyway.** `ddl-auto` fica em `validate` fora do dev.
- Índices explícitos nas colunas de filtro (`event_id`, `status`, `seller_id`, `email`).

### 2.4 Segurança

- Segredos **só via variável de ambiente** (`${REVENDE_JWT_SECRET}`), nunca commitados.
- Nunca logar token, senha, hash ou dado pessoal.
- Autorização é decisão de domínio: negar acesso lança exceção de domínio
  mapeada para `403` — não `IllegalArgumentException` → `400`.
- Falha de autenticação nunca é silenciosa (`catch (Exception ignored)` é proibido).
- Endpoint de escrita sem checagem de dono/permissão não passa em review.

### 2.5 Testes — obrigatórios

Nenhuma task de feature ou refactor é concluída sem teste. Pirâmide:

1. **Domínio** — JUnit puro, sem Spring, sem mock. Cobre regra e caso de borda. Rápido.
2. **Use case** — mocks dos ports de saída (Mockito). Cobre orquestração e erro.
3. **Adapter** — `@DataJpaTest` para persistência, `@WebMvcTest` para controller.
4. **Ponta a ponta** — `@SpringBootTest` + MockMvc nos fluxos críticos (login, publicar anúncio).

Nome do teste descreve comportamento: `shouldRejectCancelWhenRequesterIsNotSeller()`.
Cada bug corrigido ganha um teste que falha antes do fix.

---

## 3. Checklist antes de encerrar uma task

- [ ] `mvn -q verify` passa (compila + testes + ArchUnit).
- [ ] Nenhum import de framework entrou em `domain/`.
- [ ] Regra de negócio nova está no domínio, com teste unitário.
- [ ] Caminho de erro tratado e com status HTTP correto.
- [ ] Nenhum segredo, credencial ou dado pessoal em código, log ou config versionada.
- [ ] Listagem nova é paginada e sem N+1.
- [ ] `README.md` atualizado se endpoint, env var ou setup mudou.
- [ ] Diff enxuto: sem código morto, sem TODO vago, sem reformatação não relacionada.

---

## 4. Dívidas conhecidas (estado atual)

Levantadas na análise do código atual — trate quando a task encostar nelas, e **não replique o padrão**:

1. **Zero testes.** `src/test/java` está vazio.
2. Segredo JWT tem **default de dev embutido** no `application.yml`; produção depende de
   `REVENDE_JWT_SECRET` estar setado — não há validação de que foi trocado no boot.
3. `ddl-auto: update` e sem Flyway — schema não versionado.
4. Ownership de anúncio lança `IllegalArgumentException` → responde **400 em vez de 403**
   (`ListingService.ensureOwner`).
5. `JwtAuthFilter` tem `catch (Exception ignored)` — falha de token some sem trace.
6. `TicketListing` usa `FetchType.EAGER` em `event` e `seller` → N+1 nas listagens.
7. Nenhum `@Transactional` nos services.
8. `GET /api/events` e `/api/listings` sem paginação.
9. `POST /api/events` exige apenas JWT — **qualquer usuário cria evento**, sem papel/role.
10. Entidades JPA anêmicas com setters públicos e sem invariantes.

---

## 5. Comandos

```bash
mvn spring-boot:run     # sobe em http://localhost:8080
mvn -q verify           # build + testes (gate antes de entregar)
mvn test                # só testes
```
