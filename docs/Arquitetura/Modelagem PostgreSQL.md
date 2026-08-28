---
tags: [dados, referencia, postgres]
---

# Modelagem PostgreSQL

Regras práticas para o schema. Contrato normativo em [[CLAUDE]] §3.

## Tipos

| Precisa de | Use | Nunca |
|---|---|---|
| Dinheiro | `NUMERIC(12,2)` ou precisão justificada | `float`, `double`, `real` |
| Instante (criação, pagamento) | `TIMESTAMPTZ` | `TIMESTAMP` sem fuso |
| Data-hora de calendário (a hora do show) | `TIMESTAMP` | |
| Texto | `TEXT`, ou `VARCHAR(n)` com `n` justificado | `VARCHAR(255)` por hábito |
| Inteiro | `BIGINT` | |
| Chave primária | `BIGINT IDENTITY` / `BIGSERIAL` | |
| ID que aparece em URL pública | `UUID` (v7, ordenável) | sequencial, que vaza volume |

`TIMESTAMPTZ` guarda um instante absoluto; `TIMESTAMP` guarda um relógio de parede sem
contexto. No Revende, `TicketListing.createdAt` é instante — `TIMESTAMPTZ`. `Event.date` é
o horário local do evento — `TIMESTAMP` está certo.

## Índices

**O Postgres não cria índice em chave estrangeira automaticamente.** Isso surpreende quem
vem do MySQL, e é a causa silenciosa mais comum de consulta lenta.

Crie índice para: chave estrangeira, filtro frequente, coluna de ordenação e chave de junção.
Fora disso, custa escrita e espaço sem devolver nada.

Índice composto segue a **ordem em que você filtra**: `(event_id, status)` serve consulta que
filtra por evento e status, e também a que filtra só por evento — mas não a que filtra só
por status.

## Nomes

`snake_case` sempre. Identificador sem aspas é convertido para minúsculo pelo Postgres, e
nome com aspas e maiúscula obriga aspas para sempre, em toda consulta. Tabela no plural.

## Constraint é regra de negócio

O banco é a última linha de defesa e a única que sobrevive a bug de aplicação. Validação no
Java não substitui: **corrida entre duas requisições passa pela validação e só a constraint
segura** — foi assim que a v1 podia criar dois usuários com o mesmo e-mail sob concorrência,
mesmo tendo o `existsByEmail`.

## Ferramentas que valem aprender

- `EXPLAIN (ANALYZE, BUFFERS)` — a única forma honesta de saber por que uma consulta é lenta
- `VACUUM` — manutenção, não emergência

## Fontes

- [PostgreSQL Best Practices for Production](https://medium.com/@pothiq/postgresql-in-production-a-beginner-to-pro-guide-82db452ffc88)
- [Top 10 Database Schema Design Best Practices — Bytebase](https://www.bytebase.com/blog/top-database-schema-design-best-practices/)
- [Best Practices for Database Schema Design in PostgreSQL — Reintech](https://reintech.io/blog/best-practices-database-schema-design-postgresql)
