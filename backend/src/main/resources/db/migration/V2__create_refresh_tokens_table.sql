CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY,
                                token VARCHAR(500) NOT NULL UNIQUE,
                                expires_at TIMESTAMP NOT NULL,
                                user_id UUID NOT NULL,
                                created_at TIMESTAMP NOT NULL,
                                updated_at TIMESTAMP NOT NULL,

                                CONSTRAINT fk_refresh_token_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE
);