---
tags: [moc, caso-de-uso]
---

# Índice de Casos de Uso

Um caso de uso por operação da API. Cada nota diz **quem faz, o que precisa ser verdade
antes, o que acontece, quais regras se aplicam e o que pode dar errado**.

| # | Caso de uso | Endpoint | Auth |
|---|---|---|---|
| UC-01 | [[UC-01 Registrar conta]] | `POST /api/auth/register` | público |
| UC-02 | [[UC-02 Autenticar]] | `POST /api/auth/login` | público |
| UC-03 | [[UC-03 Listar eventos]] | `GET /api/events` | público |
| UC-04 | [[UC-04 Detalhar evento]] | `GET /api/events/{id}` | público |
| UC-05 | [[UC-05 Cadastrar evento]] | `POST /api/events` | JWT |
| UC-06 | [[UC-06 Listar anúncios ativos]] | `GET /api/listings` | público |
| UC-07 | [[UC-07 Detalhar anúncio]] | `GET /api/listings/{id}` | público |
| UC-08 | [[UC-08 Listar meus anúncios]] | `GET /api/listings/me` | JWT |
| UC-09 | [[UC-09 Publicar anúncio]] | `POST /api/listings` | JWT |
| UC-10 | [[UC-10 Marcar anúncio como vendido]] | `PATCH /api/listings/{id}/sold` | JWT (dono) |
| UC-11 | [[UC-11 Cancelar anúncio]] | `DELETE /api/listings/{id}` | JWT (dono) |

## O que ainda não existe

Comprar, reservar, contatar vendedor, avaliar, editar anúncio, editar evento, trocar senha,
excluir conta. Ver [[RN-014 Plataforma não intermedia pagamento]] e [[Perguntas em Aberto]].

Voltar: [[Home]] · Ver [[Índice de Regras]] · [[Arquitetura Hexagonal]]
