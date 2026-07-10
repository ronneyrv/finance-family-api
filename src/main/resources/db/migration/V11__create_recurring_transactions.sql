CREATE TABLE recurring_transactions (
    id UUID PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    day_of_month INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    active BOOLEAN NOT NULL,

    user_id UUID NOT NULL,
    category_id UUID NOT NULL,
    sub_category_id UUID,

    CONSTRAINT fk_recurring_transaction_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT fk_recurring_transaction_category
        FOREIGN KEY (category_id)
            REFERENCES categories(id),

    CONSTRAINT fk_recurring_transaction_sub_category
        FOREIGN KEY (sub_category_id)
            REFERENCES sub_categories(id),

    CONSTRAINT chk_recurring_transaction_day_of_month
        CHECK (day_of_month BETWEEN 1 AND 31),

    CONSTRAINT chk_recurring_transaction_date_range
        CHECK (end_date IS NULL OR end_date >= start_date)
);