CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,

    CONSTRAINT uk_category_name_type
        UNIQUE (name, type)
);