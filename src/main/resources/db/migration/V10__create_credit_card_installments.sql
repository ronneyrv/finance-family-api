CREATE TABLE credit_card_installments (
    id UUID PRIMARY KEY,
    amount NUMERIC(15,2) NOT NULL,
    installment_number INTEGER NOT NULL,
    invoice_month INTEGER NOT NULL,
    invoice_year INTEGER NOT NULL,
    paid BOOLEAN NOT NULL DEFAULT FALSE,
    paid_at DATE,
    purchase_id UUID NOT NULL,

    CONSTRAINT fk_installment_purchase
        FOREIGN KEY (purchase_id)
            REFERENCES purchases(id)
);

CREATE INDEX idx_credit_card_installments_purchase_id
    ON credit_card_installments(purchase_id);

CREATE INDEX idx_credit_card_installments_invoice
    ON credit_card_installments(invoice_year, invoice_month);
