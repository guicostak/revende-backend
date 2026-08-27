---
tags: [regra-de-negocio, catalog, implementada]
contexto: Catalog
codigo: src/main/java/com/revende/backend/service/EventService.java
---

# RN-013 — Busca de eventos por cidade ou nome

> A busca aceita `?city=` **ou** `?name=`, nunca os dois juntos: **cidade tem precedência**.
> Sem filtro, devolve o catálogo inteiro.

## Comportamento exato

```
city preenchido            → findByCityIgnoreCase(city)     [igualdade exata, sem acento tratado]
city vazio + name          → findByNameContainingIgnoreCase(name)  [contém]
nenhum dos dois            → findAll()
city E name preenchidos    → name é SILENCIOSAMENTE IGNORADO
```

## Por quê

Busca mínima para a primeira versão. A precedência não foi decidida por produto —
é consequência do `if/else if` em `EventService.list`.

## Furos

1. **`city` + `name` juntos**: o usuário filtra por dois critérios e recebe resultado de um.
   Nenhum aviso. É comportamento surpreendente, não regra.
2. `city` é igualdade exata: "sao paulo" não acha "São Paulo". Sem normalização de acento.
3. Sem paginação: `findAll()` cru. Dívida nº 8 em [[Dívidas Técnicas]].
4. Sem filtro por data, categoria ou faixa de preço — citados no `README` como próximos passos.

## Alvo

Um único port de saída com objeto de critérios:

```java
Page<Event> search(EventSearchCriteria criteria, Pageable pageable);
```

criteria combina os filtros com `AND`, e o retorno é sempre paginado.

## Testes esperados

- `?city=São Paulo` → só eventos daquela cidade
- `?name=cold` → casa por trecho, sem diferenciar maiúsculas
- **`?city=X&name=Y` → definir e testar o comportamento esperado** (hoje indefinido)

Aplicada em [[UC-03 Listar eventos]].
