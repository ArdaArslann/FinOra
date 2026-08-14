CREATE TABLE receipt_extractions
(
    id UUID PRIMARY KEY,

    receipt_id UUID NOT NULL UNIQUE,

    merchant_name VARCHAR(255),

    total_amount NUMERIC(19, 2),

    transaction_date DATE,

    currency VARCHAR(10),

    suggested_category VARCHAR(100),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_receipt_extraction_receipt
        FOREIGN KEY (receipt_id)
            REFERENCES receipts(id)
            ON DELETE CASCADE
);