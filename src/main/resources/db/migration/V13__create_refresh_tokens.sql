CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(36) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL,
    user_id UUID NOT NULL,

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);