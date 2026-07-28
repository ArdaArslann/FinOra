CREATE TABLE receipts
(
    id UUID PRIMARY KEY,

    original_file_name VARCHAR(255) NOT NULL,

    storage_key VARCHAR(255) NOT NULL UNIQUE,

    content_type VARCHAR(100) NOT NULL,

    file_size BIGINT NOT NULL,

    status VARCHAR(30) NOT NULL,

    uploaded_at TIMESTAMP NOT NULL,

    user_id UUID NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_receipt_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
);