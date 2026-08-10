ALTER TABLE receipts
    ADD COLUMN transaction_id UUID;

ALTER TABLE receipts
    ADD CONSTRAINT fk_receipt_transaction
        FOREIGN KEY (transaction_id)
            REFERENCES transactions(id);