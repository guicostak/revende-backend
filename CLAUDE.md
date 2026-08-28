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

## 3. Modelagem de dados e desenho de entidades

> O domínio foi zerado para ser remodelado. Esta seção é a régua dessa remodelagem.

### 3.1 Comece pelo agregado, não pela tabela

A pergunta não é "quais tabelas preciso", é **"o que precisa mudar junto, numa transação, para o dado nunca ficar inconsistente?"**. Esse conjunto é um agregado, e ele tem uma raiz — a única classe que o mundo de fora enxerga.

Quatro regras que definem a fronteira:

1. **Uma transação altera um agregado.** Se uma operação precisa gravar dois agregados atomicamente, ou a fronteira está errada, ou falta um evento entre eles.
2. **Agregados se referenciam por ID, nunca por objeto.** `TicketListing` guarda `SellerId`, não `User`. Isso impede o grafo inteiro de ser carregado por acidente e mantém os contextos desacoplados.
3. **Invariante mora dentro da raiz.** Se uma regra precisa olhar dois agregados para decidir, ela não é invariante — é caso de uso.
4. **Menor é melhor.** Agregado grande vira ponto de contenção sob concorrência e carrega dado que ninguém pediu.

### 3.2 Do agregado para o schema

| Decisão | Regra |
|---|---|
| Chave primária | `BIGSERIAL` (ou UUID v7 se o ID vazar para URL pública). Nunca chave natural composta |
| Nome de tabela | plural, snake_case: `ticket_listings` |
| Dinheiro | `NUMERIC(19,2)`. **Nunca** `float`/`double`. E guarde a moeda se um dia houver mais de uma |
| Data com fuso | `TIMESTAMPTZ` para instante (criação, pagamento). `TIMESTAMP` só para data-hora local de calendário |
| Enum | `VARCHAR` + `CHECK`, não o tipo `ENUM` do Postgres — renomear valor de enum nativo exige migração dolorosa |
| Booleano opcional | evite `NULL` em booleano: três estados onde a regra prevê dois |
| Texto | `VARCHAR(n)` com `n` justificado, ou `TEXT`. Não invente 255 por hábito |

### 3.3 Constraint é regra de negócio, não enfeite

O banco é a última linha de defesa e a única que sobrevive a bug de aplicação. Cada invariante que **puder** virar constraint, deve:

- `NOT NULL` em tudo que é obrigatório
- `UNIQUE` em identidade de negócio (e-mail)
- `FOREIGN KEY` em toda referência, com `ON DELETE` explícito e pensado
- `CHECK` para faixa válida: `price > 0`, `quantity >= 1`

Validação no Java **não substitui** constraint no banco: corrida entre duas requisições passa pela validação e só a constraint segura.

### 3.4 Índices

Índice existe para três coisas: chave estrangeira, coluna de filtro frequente e coluna de ordenação. Fora disso, custa escrita e espaço sem devolver nada.

Regra prática: **toda coluna que aparece em `WHERE` de consulta da vitrine precisa de índice**, e filtro combinado pede índice composto na ordem em que é filtrado.

### 3.5 Migrações

- Uma migração por mudança, **imutável depois de aplicada**. Corrigir migração já aplicada gera divergência entre ambientes
- `V{n}__descricao_no_imperativo.sql`
- Toda migração precisa ser segura em produção com a versão anterior da aplicação rodando: adicionar coluna nullable, preencher, depois tornar obrigatória. Três passos, não um
- `ddl-auto` fica em `validate`. O Hibernate confere, nunca decide
- **Nunca** `DROP COLUMN` no mesmo release que para de usá-la. Deixe uma versão de distância para conseguir voltar atrás

### 3.6 Performance é modelagem, não otimização

- `@ManyToOne` é **sempre** `LAZY`. O default `EAGER` do `@ManyToOne` é a maior fonte de N+1 em Spring Data
- Consulta de listagem usa `JOIN FETCH` ou projeção, nunca navegação de objeto
- Toda listagem é paginada, com ordenação explícita e determinística
- Suspeita de N+1 se resolve contando queries em teste, não olhando o código

---

## 4. Checklist antes de encerrar uma task

- [ ] `mvn -q verify` passa (compila + testes + ArchUnit).
- [ ] Nenhum import de framework entrou em `domain/`.
- [ ] Regra de negócio nova está no domínio, com teste unitário.
- [ ] Caminho de erro tratado e com status HTTP correto.
- [ ] Nenhum segredo, credencial ou dado pessoal em código, log ou config versionada.
- [ ] Listagem nova é paginada e sem N+1.
- [ ] `README.md` atualizado se endpoint, env var ou setup mudou.
- [ ] Diff enxuto: sem código morto, sem TODO vago, sem reformatação não relacionada.

---

## 5. Estado atual e lições da versão anterior

**O domínio foi zerado em 2026-08-27** para ser remodelado em hexagonal. De pé hoje:
ponto de entrada Spring Boot, teste de subida contra Postgres real, pipeline de 7
estágios, Docker, Flyway e configuração por ambiente. Zero código de negócio.

O código anterior está no histórico, em `c300a61`. Ele funcionava, e mesmo assim
carregava os defeitos abaixo. **Não repita nenhum** — cada um custou uma nota em
`docs/` explicando o estrago.

| Defeito da v1 | Como evitar agora |
|---|---|
| Transição de status não validada: dava para cancelar anúncio já vendido e apagar o registro da venda | Transição é método do agregado, com estado terminal recusando com **409** |
| Falta de posse respondia **400** | Exceção de domínio própria → **403** |
| `AuthenticationException` sem handler: senha errada devolvia **500** | Handler dedicado → **401**, mensagem genérica |
| Rota `/me` casava com padrão público: sem token dava NPE → **500** | Regra de autorização explícita por rota, nunca por coringa |
| Entidades anêmicas com setter público | Agregado sem setter; mudança de estado só por método com nome de negócio |
| `EAGER` em `@ManyToOne` | Sempre `LAZY` |
| Sem paginação | Toda listagem paginada desde a primeira versão |
| Busca ignorava um filtro em silêncio | Critério combinado explícito, ou recusa clara |
| Zero testes | Regra de domínio nasce com teste unitário |

Pendências de infraestrutura que atravessaram a remodelagem:

1. `V1__baseline.sql` descreve o **modelo antigo**. Ao definir as entidades novas, ela
   precisa ser reescrita, senão `ddl-auto: validate` acusa divergência.
2. Spring Security está no classpath **sem configuração**: hoje toda rota fica sob HTTP
   Basic com senha aleatória, inclusive `/actuator/health` — o que quebra as sondas.
3. O quality gate do Sonar mede código novo; com o domínio zerado, a régua de cobertura
   vai apertar de verdade a partir da primeira classe escrita.

## 6. Comandos

```bash
mvn spring-boot:run     # sobe em http://localhost:8080
mvn -q verify           # build + testes (gate antes de entregar)
mvn test                # só testes
```
