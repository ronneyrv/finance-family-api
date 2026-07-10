CREATE TABLE purchases (
    id UUID PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    total_amount NUMERIC(15,2) NOT NULL,
    purchase_date DATE NOT NULL,
    installment_count INTEGER NOT NULL,
    credit_card_id UUID NOT NULL,

    CONSTRAINT fk_purchase_credit_card
        FOREIGN KEY (credit_card_id)
            REFERENCES credit_cards(id)
);

CREATE INDEX idx_purchases_credit_card_id
    ON purchases(credit_card_id);
