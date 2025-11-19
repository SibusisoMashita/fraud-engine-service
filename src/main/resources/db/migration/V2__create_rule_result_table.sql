CREATE TABLE rule_result (
    result_id UUID PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    passed BOOLEAN NOT NULL,
    reason TEXT,
    score INT,

    CONSTRAINT fk_rule_tx FOREIGN KEY (transaction_id)
        REFERENCES transaction (transaction_id)
        ON DELETE CASCADE
);
