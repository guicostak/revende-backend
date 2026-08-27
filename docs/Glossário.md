---
tags: [dominio, glossario]
---

# Glossário

Vocabulário único do projeto. Estes termos devem aparecer **iguais** na conversa com
o negócio, nas notas deste vault e nos nomes de classe do código (traduzidos para inglês).

| Português (negócio) | Inglês (código) | Significado |
|---|---|---|
| Usuário | `User` | Pessoa com conta. Pode vender e navegar. Ver [[Usuário]] |
| Vendedor | `Seller` | Papel do usuário que publica um anúncio. Não é entidade separada |
| Comprador | `Buyer` | Quem procura ingresso. **Ainda não existe no sistema** — ver [[RN-014 Plataforma não intermedia pagamento]] |
| Evento | `Event` | Show, festival ou jogo com data, local e cidade. Ver [[Evento]] |
| Anúncio | `TicketListing` | Oferta de revenda de um lote de ingressos. Ver [[Anúncio de Ingresso]] |
| Lote | `quantity` | Quantidade de ingressos de um anúncio, vendida em bloco. Ver [[RN-009 Lote de ingressos é indivisível]] |
| Preço original | `originalPrice` | Quanto o vendedor pagou na compra oficial |
| Preço de revenda | `price` | Quanto o vendedor pede. Ver [[RN-004 Preço de revenda é livre]] |
| Tipo de ingresso | `TicketType` | Inteira, meia, VIP ou backstage. Ver [[Tipo de Ingresso]] |
| Situação do anúncio | `ListingStatus` | Ativo, vendido ou cancelado. Ver [[Status do Anúncio]] |
| Vitrine | — | Conjunto de anúncios ativos visíveis sem login. Ver [[RN-008 Vitrine mostra apenas anúncios ativos]] |

> [!warning] Termos proibidos
> **"Ticket"** sozinho é ambíguo: pode ser o ingresso físico ou o anúncio. Use `TicketListing`
> para o anúncio e reserve `Ticket` para quando existir o ingresso individual como entidade.
> **"Deletar anúncio"** também: a operação é *cancelar* — ver [[RN-012 Cancelamento é lógico]].
