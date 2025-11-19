CREATE TABLE fraud_decision (
    decision_id UUID PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL UNIQUE,
    is_fraud BOOLEAN NOT NULL,
    severity INT NOT NULL,
    evaluated_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_decision_tx FOREIGN KEY (transaction_id)
        REFERENCES transaction (transaction_id)
        ON DELETE CASCADE
);
