CREATE TABLE financial_accounts (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    initial_balance NUMERIC(15, 2) NOT NULL,
    user_id UUID NOT NULL,

    CONSTRAINT fk_financial_accounts_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
);

CREATE INDEX idx_financial_accounts_user_id
    ON financial_accounts(user_id);