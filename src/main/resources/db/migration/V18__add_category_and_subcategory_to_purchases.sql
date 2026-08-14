ALTER TABLE purchases
    ADD COLUMN category_id UUID,
    ADD COLUMN sub_category_id UUID;

ALTER TABLE purchases
    ADD CONSTRAINT fk_purchase_category
        FOREIGN KEY (category_id)
            REFERENCES categories(id);

ALTER TABLE purchases
    ADD CONSTRAINT fk_purchase_sub_category
        FOREIGN KEY (sub_category_id)
            REFERENCES sub_categories(id);