---
tags: [dominio, enum, marketplace]
contexto: Marketplace
codigo: src/main/java/com/revende/backend/model/TicketType.java
---

# Tipo de Ingresso

`TicketType` — categoria do ingresso anunciado. Persistido como `STRING`.

| Valor | Significado |
|---|---|
| `INTEIRA` | Ingresso comum, preço cheio |
| `MEIA` | Meia-entrada (estudante, idoso, etc.) |
| `VIP` | Área VIP / camarote |
| `BACKSTAGE` | Acesso a bastidores |

## Pontos em aberto

- **`MEIA` é vinculada a documento**: revender meia-entrada para alguém sem direito ao
  benefício é problemático. Hoje não há checagem nem aviso. Ver [[Perguntas em Aberto]].
- O tipo é **declarado pelo vendedor**, sem validação contra o ingresso real.
- Não há relação entre tipo e faixa de preço esperada.

Usado por [[Anúncio de Ingresso]] no [[Contexto Marketplace]].
