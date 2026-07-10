CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    transaction_date DATE NOT NULL,
    type VARCHAR(20) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,

    user_id UUID NOT NULL,
    category_id UUID NOT NULL,
    sub_category_id UUID,
    financial_account_id UUID NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_transaction_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT fk_transaction_category
        FOREIGN KEY (category_id)
            REFERENCES categories(id),

    CONSTRAINT fk_transaction_sub_category
        FOREIGN KEY (sub_category_id)
            REFERENCES sub_categories(id),

    CONSTRAINT fk_transaction_financial_account
        FOREIGN KEY (financial_account_id)
            REFERENCES financial_accounts(id)
);

CREATE INDEX idx_transactions_user_id
    ON transactions(user_id);

CREATE INDEX idx_transactions_financial_account_id
    ON transactions(financial_account_id);
