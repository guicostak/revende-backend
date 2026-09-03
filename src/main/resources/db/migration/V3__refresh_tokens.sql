
CREATE TABLE refresh_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP(6) WITH TIME ZONE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash)
);

CREATE INDEX ix_refresh_tokens_user ON refresh_tokens (user_id);

CREATE INDEX ix_refresh_tokens_expires_at ON refresh_tokens (expires_at);
