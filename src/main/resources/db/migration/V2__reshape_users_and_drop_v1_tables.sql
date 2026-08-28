-- O domínio foi remodelado: users ganha os dados de vendedor e de perfil, e as tabelas
-- do modelo antigo saem porque não há mais entidade mapeando para elas.

DROP TABLE IF EXISTS ticket_listings;
DROP TABLE IF EXISTS events;

ALTER TABLE users RENAME COLUMN password TO password_hash;

ALTER TABLE users
    ALTER COLUMN name  TYPE VARCHAR(120),
    ALTER COLUMN email TYPE VARCHAR(320),
    ALTER COLUMN phone TYPE VARCHAR(20);

ALTER TABLE users
    ADD COLUMN status          VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN profile_picture VARCHAR(500),
    ADD COLUMN cpf             VARCHAR(11),
    ADD COLUMN pix_key         VARCHAR(140),
    ADD COLUMN pix_key_type    VARCHAR(10),
    ADD COLUMN street          VARCHAR(200),
    ADD COLUMN number          VARCHAR(20),
    ADD COLUMN complement      VARCHAR(100),
    ADD COLUMN district        VARCHAR(100),
    ADD COLUMN city            VARCHAR(100),
    ADD COLUMN state           VARCHAR(2),
    ADD COLUMN zip_code        VARCHAR(8),
    ADD COLUMN updated_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT now();

ALTER TABLE users ADD CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'BLOCKED'));
ALTER TABLE users ADD CONSTRAINT ck_users_pix_key_type
    CHECK (pix_key_type IS NULL OR pix_key_type IN ('CPF', 'EMAIL', 'PHONE', 'RANDOM'));

CREATE UNIQUE INDEX uk_users_cpf ON users (cpf) WHERE cpf IS NOT NULL;
