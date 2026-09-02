-- Refresh token vive no banco, não em cache de instância: ele precisa ser revogável, e
-- revogação só funciona com fonte única. Com cache local, a instância A revoga e a B
-- continua aceitando — e o balanceamento torna a falha intermitente.

CREATE TABLE refresh_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    -- Só o hash. Vazamento de backup ou de log não entrega sessão de ninguém.
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    -- Preenchido na rotação e no logout. Token revogado NÃO é apagado: é a apresentação
    -- de um token já revogado que denuncia roubo, e apagar destruiria essa evidência.
    revoked_at TIMESTAMP(6) WITH TIME ZONE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash)
);

-- A detecção de reuso revoga todos os tokens do usuário de uma vez; sem este índice
-- isso é varredura completa a cada incidente.
CREATE INDEX ix_refresh_tokens_user ON refresh_tokens (user_id);

-- Limpeza de expirados varre por data.
CREATE INDEX ix_refresh_tokens_expires_at ON refresh_tokens (expires_at);
