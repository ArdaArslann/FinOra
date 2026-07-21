CREATE TABLE transactions (

                              id UUID PRIMARY KEY,

                              amount NUMERIC(19,2) NOT NULL,

                              type VARCHAR(20) NOT NULL,

                              description VARCHAR(255),

                              transaction_date DATE NOT NULL,

                              category_id UUID NOT NULL,

                              user_id UUID NOT NULL,

                              created_at TIMESTAMP NOT NULL,

                              updated_at TIMESTAMP NOT NULL,

                              CONSTRAINT fk_transaction_category
                                  FOREIGN KEY (category_id)
                                      REFERENCES categories(id),

                              CONSTRAINT fk_transaction_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES users(id)

);