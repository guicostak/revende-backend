-- Baseline do schema. Reproduz o mapeamento JPA existente e adiciona os índices
-- que faltavam nas colunas de filtro.

CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    phone      VARCHAR(255),
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE events (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    date        TIMESTAMP(6) NOT NULL,
    venue       VARCHAR(255) NOT NULL,
    city        VARCHAR(255) NOT NULL,
    category    VARCHAR(255),
    image_url   VARCHAR(255)
);

CREATE TABLE ticket_listings (
    id             BIGSERIAL PRIMARY KEY,
    event_id       BIGINT NOT NULL,
    seller_id      BIGINT NOT NULL,
    ticket_type    VARCHAR(255) NOT NULL,
    original_price NUMERIC(38, 2) NOT NULL,
    price          NUMERIC(38, 2) NOT NULL,
    quantity       INTEGER NOT NULL,
    description    VARCHAR(1000),
    status         VARCHAR(255) NOT NULL,
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_listing_event  FOREIGN KEY (event_id)  REFERENCES events (id),
    CONSTRAINT fk_listing_seller FOREIGN KEY (seller_id) REFERENCES users (id)
);

-- Índices de filtro. Sem eles, toda consulta da vitrine é varredura completa.
CREATE INDEX idx_events_city           ON events (city);
CREATE INDEX idx_listings_status       ON ticket_listings (status);
CREATE INDEX idx_listings_event_status ON ticket_listings (event_id, status);
CREATE INDEX idx_listings_seller       ON ticket_listings (seller_id);
