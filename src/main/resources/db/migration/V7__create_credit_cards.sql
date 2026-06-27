CREATE TABLE credit_cards (
  id UUID PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  credit_limit NUMERIC(15,2) NOT NULL,
  closing_day INTEGER NOT NULL,
  due_day INTEGER NOT NULL,
  user_id UUID NOT NULL,

  CONSTRAINT fk_credit_card_user
      FOREIGN KEY (user_id)
          REFERENCES users(id)
);