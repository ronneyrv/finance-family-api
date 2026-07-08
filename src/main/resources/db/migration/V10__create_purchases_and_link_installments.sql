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

ALTER TABLE credit_card_installments
    ADD COLUMN purchase_id UUID;

ALTER TABLE credit_card_installments
    ADD CONSTRAINT fk_installment_purchase
        FOREIGN KEY (purchase_id)
            REFERENCES purchases(id);

ALTER TABLE credit_card_installments
DROP COLUMN description;

ALTER TABLE credit_card_installments
DROP COLUMN total_installments;

ALTER TABLE credit_card_installments
DROP COLUMN purchase_date;

ALTER TABLE credit_card_installments
DROP COLUMN credit_card_id;

ALTER TABLE credit_card_installments
    ALTER COLUMN purchase_id SET NOT NULL;