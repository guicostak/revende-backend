# Revende · Backend

API REST do **Revende**, um marketplace de revenda de ingressos (estilo Sympla).
Spring Boot 3 · Java 21 · Maven · Spring Security (JWT) · JPA · H2 (PostgreSQL opcional).

## Rodando

Requer **Java 21** e **Maven**.

```bash
cd revende-backend
mvn spring-boot:run
```

API em `http://localhost:8080`. O banco H2 sobe **vazio** a cada start:
crie sua conta em `POST /api/auth/register` e um evento em `POST /api/events`.

## Endpoints

| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| POST | `/api/auth/register` | público | Cria conta (retorna JWT) |
| POST | `/api/auth/login` | público | Login (retorna JWT) |
| GET | `/api/events` | público | Lista eventos (`?city=` `?name=`) |
| GET | `/api/events/{id}` | público | Detalhe do evento |
| POST | `/api/events` | JWT | Cria evento |
| GET | `/api/listings` | público | Lista anúncios ativos (`?eventId=`) |
| GET | `/api/listings/{id}` | público | Detalhe do anúncio |
| GET | `/api/listings/me` | JWT | Meus anúncios |
| POST | `/api/listings` | JWT | Publica ingresso para revenda |
| PATCH | `/api/listings/{id}/sold` | JWT (dono) | Marca como vendido |
| DELETE | `/api/listings/{id}` | JWT (dono) | Cancela anúncio |

Envie o token em `Authorization: Bearer <token>`.

## Estrutura

```
com.revende.backend
├── config       SecurityConfig
├── controller   Auth/Event/Listing controllers
├── dto          records de request/response
├── model        User, Event, TicketListing, enums
├── repository   Spring Data JPA
├── security     JwtService, JwtAuthFilter, UserDetailsService
├── service      regras de negócio
└── exception    NotFoundException, GlobalExceptionHandler
```

## PostgreSQL (opcional)

No `application.yml`, comente o bloco H2 e descomente o bloco PostgreSQL.

## Próximos passos sugeridos

- Entidade `Order`/`Purchase` para concretizar a compra
- Pagamento (Pix / cartão)
- Upload de imagem do evento
- Avaliações de vendedor
- Paginação e filtros avançados (data, categoria, faixa de preço)
