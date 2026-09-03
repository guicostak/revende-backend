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

## 1. Arquitetura (Ports & Adapters, pragmática)

### 1.1 A regra única

**A dependência sempre aponta para dentro.** O domínio não conhece a web, não conhece o
adapter, não conhece o mundo de fora. A aplicação orquestra; os adapters implementam.

O que **não** herdamos do hexagonal ortodoxo: a exigência de domínio livre de framework e a
duplicação entre entidade de domínio e entidade JPA. Ambas custam mais do que devolvem
nesta escala.

```
        HTTP / CLI / Scheduler                   JPA / SMTP / HTTP client
                 │                                          │
        ┌────────▼─────────┐                     ┌──────────▼─────────┐
        │   adapter/web    │                     │ adapter/persistence│
        └────────┬─────────┘                     └──────────▲─────────┘
                 │ chama o port                   implementa │ o port
        ┌────────▼──────────────────────────────────────────┴─────────┐
        │                       application                            │
        │            (use cases: orquestra, transaciona)               │
        └────────────────────────────┬─────────────────────────────────┘
                                     │ usa
                        ┌────────────▼────────────┐
                        │      entity · model     │
                        └─────────────────────────┘
```

### 1.2 Estrutura de pacotes

```
com.revende.backend
├── shared/                          # kernel comum
└── <contexto>/                      # identity · catalog · marketplace · payments
     ├── entity/                     # classes @Entity — o que vira tabela
     ├── model/                      # enums e tipos de apoio que não viram tabela
     ├── application/
     │   ├── port/                  # interfaces: casos de uso e o que o domínio
     │   │                          #   precisa de fora
     │   └── service/                # implementa os casos de uso, usa os ports
     └── adapter/
         ├── web/                    # controller + DTOs
         ├── persistence/            # Spring Data, implementa os ports
         └── security/               # hash, emissão de token
```

**`entity/` e `model/`:** uma classe por conceito, sem mapper entre domínio e persistência.
A entidade JPA *é* a entidade de domínio. O que separa os dois pacotes é ser tabela ou não.

### 1.3 O que pode existir em cada camada

| Camada | Pode | **Não pode** |
|---|---|---|
| `entity/`, `model/` | `@Entity`, Lombok, enums, regras da própria entidade | Tipos HTTP, chamada a repositório, conhecer adapter |
| `application/` | `@Service`, `@Transactional`, ports, orquestração | `ResponseEntity`, `Authentication`, SQL |
| `adapter/web` | `@RestController`, DTOs, `@Valid`, mapeamento HTTP | Regra de negócio, acesso direto a repositório |
| `adapter/persistence` | Spring Data, consultas | Regra de negócio, decisão de fluxo |
| `adapter/security` | hash de senha, emissão de token | Regra de negócio, decisão de fluxo |

### 1.4 Regras não-negociáveis

1. **Todo caso de uso é um port de entrada** (`application/port`). O controller depende
   da interface, nunca da classe concreta.
2. **Toda saída passa por um port** (`application/port`), implementado no adapter.
   O `application` não importa `JpaRepository`.
3. **DTO de web não entra na aplicação e entidade não sai pela web.** O mapeamento acontece
   na borda.
4. **Identidade do usuário chega como tipo próprio**, não como `String email` vindo de
   `Authentication.getName()`.
5. Um contexto só fala com outro através de um **port de saída**, nunca importando o
   service do outro.
6. **Validação de formato fica na borda**, com Bean Validation nos DTOs. A entidade não
   valida a si mesma.

### 1.5 Ordem de construção

Um contexto por vez. Comece por `identity`, que é o que destrava todo o resto.

| # | Passo | Só avance quando |
|---|---|---|
| 1 | Entidade `@Entity` + enums | Migração Flyway aplica e `ddl-auto: validate` aceita |
| 2 | Repositório Spring Data + port de saída | O caso de uso compila sem saber qual banco existe |
| 3 | Port de entrada + service por caso de uso | Teste com mock dos ports cobre orquestração e erro |
| 4 | Controller, DTOs e validação | Teste de API cobre o status de cada caminho de erro |

### 1.6 Como proteger a arquitetura

ArchUnit (`com.tngtech.archunit:archunit-junit5`, escopo `test`) com testes que falham o
build se:

- `entity`/`model` importarem `org.springframework.web` ou tipos HTTP
- `application` importar `adapter` ou `JpaRepository`
- houver ciclo entre pacotes

Repare no que **não** está na lista: `jakarta.persistence` na entidade é esperado, porque a
entidade JPA é a entidade de domínio. A regra protege a **direção da dependência**, não a
pureza de framework.

**Esse teste é o guardião — não desabilite para "passar" uma task.**

## 2. Padrões de código

### 2.1 Geral

- Java 21: use `record` para DTOs, `sealed` para hierarquias fechadas, pattern matching.
- **Injeção por construtor**, sempre. Nunca `@Autowired` em campo.
- Entidades usam **Lombok**: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
  Exclua relações e campos sensíveis do `toString` — ver `docs/Arquitetura/Armadilhas do Spring Data JPA`.
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

> **Aprofundamento** (não normativo, com fontes): `docs/Arquitetura/` — `Desenho de Agregados`
> para as regras de Vernon, `Modelagem PostgreSQL` para o schema, `Armadilhas do Spring
> Data JPA` para o que o Hibernate castiga, `Hexagonal na prática` para onde a arquitetura
> vira cerimônia.

### 3.1 Comece pelo agregado, não pela tabela

A pergunta não é "quais tabelas preciso", é **"o que precisa mudar junto, numa transação, para o dado nunca ficar inconsistente?"**. Esse conjunto é um agregado, e ele tem uma raiz — a única classe que o mundo de fora enxerga.

Quatro regras que definem a fronteira:

1. **Uma transação altera um agregado.** Se uma operação precisa gravar dois agregados atomicamente, ou a fronteira está errada, ou falta um evento entre eles.
2. **Agregados se referenciam por ID, nunca por objeto.** `TicketListing` guarda `SellerId`, não `User`. Isso impede o grafo inteiro de ser carregado por acidente e mantém os contextos desacoplados.
3. **Regra que olha dois agregados é caso de uso**, não invariante de entidade. Validação de
   formato fica na borda; consistência entre entidades fica no service.
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
