CREATE TABLE credit_card_installments (
  id UUID PRIMARY KEY,
  description VARCHAR(255) NOT NULL,
  amount NUMERIC(15,2) NOT NULL,
  installment_number INTEGER NOT NULL,
  total_installments INTEGER NOT NULL,
  invoice_month INTEGER NOT NULL,
  invoice_year INTEGER NOT NULL,
  purchase_date DATE NOT NULL,
  paid BOOLEAN NOT NULL DEFAULT FALSE,
  credit_card_id UUID NOT NULL,
  CONSTRAINT fk_installment_credit_card
      FOREIGN KEY (credit_card_id)
          REFERENCES credit_cards(id)
);