---
tags: [dominio, agregado, catalog]
contexto: Catalog
codigo: src/main/java/com/revende/backend/model/Event.java
---

# Evento

Show, festival ou jogo com data e local. Contexto: [[Contexto Catalog]].

## Atributos

| Campo | Tipo | Obrigatório | Nota |
|---|---|---|---|
| `id` | Long | gerado | |
| `name` | String | sim | usado na busca por nome (contains, case-insensitive) |
| `description` | String(2000) | não | |
| `date` | LocalDateTime | sim | **sem fuso** — ver [[Perguntas em Aberto]] |
| `venue` | String | sim | casa de show / estádio |
| `city` | String | sim | usada na busca por cidade (igualdade, case-insensitive) |
| `category` | String | não | texto livre: "Show", "Festival". **Deveria ser enum** |
| `imageUrl` | String | não | URL externa, sem upload |

## Invariantes que faltam

- Nada impede `date` no passado.
- Não há chave natural: dois eventos idênticos podem coexistir.
- `category` como `String` livre impede filtro confiável.

## Relações

- Um evento tem N [[Anúncio de Ingresso]] — ver [[RN-011 Anúncio exige evento existente]].
- Apagar evento com anúncios não é tratado (não há endpoint de exclusão).

## Regras

[[RN-010 Quem pode cadastrar evento]] · [[RN-013 Busca de eventos por cidade ou nome]]
