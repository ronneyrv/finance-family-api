CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       name VARCHAR(150) NOT NULL,
                       email VARCHAR(200) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       household_id UUID NOT NULL,

                       CONSTRAINT fk_user_household
                           FOREIGN KEY (household_id)
                               REFERENCES households(id)
);