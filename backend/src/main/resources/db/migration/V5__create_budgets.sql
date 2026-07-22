CREATE TABLE budgets (
                         id UUID PRIMARY KEY,

                         amount NUMERIC(19,2) NOT NULL,

                         period VARCHAR(20) NOT NULL,

                         start_date DATE NOT NULL,

                         end_date DATE NOT NULL,

                         category_id UUID NOT NULL,

                         user_id UUID NOT NULL,

                         created_at TIMESTAMP NOT NULL,

                         updated_at TIMESTAMP NOT NULL,

                         CONSTRAINT fk_budget_category
                             FOREIGN KEY (category_id)
                                 REFERENCES categories(id),

                         CONSTRAINT fk_budget_user
                             FOREIGN KEY (user_id)
                                 REFERENCES users(id)
);