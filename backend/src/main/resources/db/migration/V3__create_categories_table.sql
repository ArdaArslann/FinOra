CREATE TABLE categories (
                            id UUID PRIMARY KEY,

                            name VARCHAR(50) NOT NULL,

                            icon VARCHAR(50),

                            color VARCHAR(20),

                            default_category BOOLEAN NOT NULL,

                            user_id UUID,

                            created_at TIMESTAMP NOT NULL,
                            updated_at TIMESTAMP NOT NULL,

                            CONSTRAINT fk_category_user
                                FOREIGN KEY(user_id)
                                    REFERENCES users(id)
);