CREATE TABLE goals (
   id UUID PRIMARY KEY,
   name VARCHAR(100) NOT NULL,
   target_amount NUMERIC(15,2) NOT NULL,
   current_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
   target_date DATE,
   user_id UUID NOT NULL,

   CONSTRAINT fk_goal_user
       FOREIGN KEY (user_id)
           REFERENCES users(id)
);