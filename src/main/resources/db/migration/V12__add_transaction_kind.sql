ALTER TABLE transactions
    ADD COLUMN transaction_kind VARCHAR(30) NOT NULL DEFAULT 'REGULAR';

ALTER TABLE transactions
    ALTER COLUMN transaction_kind DROP DEFAULT;

ALTER TABLE transactions
    ALTER COLUMN category_id DROP NOT NULL;