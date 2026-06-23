CREATE TABLE sub_categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category_id UUID NOT NULL,

    CONSTRAINT fk_sub_category_category
        FOREIGN KEY (category_id)
            REFERENCES categories(id)
);