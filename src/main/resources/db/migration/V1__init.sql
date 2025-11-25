-- Stores all raw transaction events that will be evaluated for fraud
CREATE TABLE transaction (
    transaction_id VARCHAR(64) PRIMARY KEY,   -- Unique identifier for the transaction
    customer_id VARCHAR(64) NOT NULL,         -- Customer associated with the transaction
    amount NUMERIC(18,2) NOT NULL,            -- Monetary value of the transaction
    timestamp TIMESTAMP NOT NULL,             -- When the transaction occurred
    merchant VARCHAR(128) NOT NULL,           -- Merchant or payee name
    location VARCHAR(64),                     -- Optional location metadata (e.g., geocode/region)
    channel VARCHAR(32) NOT NULL              -- Channel (ATM, CARD, EFT, DIGITAL, etc.)
);

-- Captures the result of each fraud rule applied to a transaction
CREATE TABLE rule_result (
    result_id UUID PRIMARY KEY,               -- Unique ID for the rule evaluation entry
    transaction_id VARCHAR(64) NOT NULL,      -- Reference to the transaction being evaluated
    rule_name VARCHAR(128) NOT NULL,          -- Name of the rule executed
    passed BOOLEAN NOT NULL,                  -- TRUE if rule passed, FALSE if it triggered
    reason TEXT,                              -- Optional explanation or details for why the rule fired
    score INT,                                -- Optional score/weight contribution towards final decision

    CONSTRAINT fk_rule_tx FOREIGN KEY (transaction_id)
        REFERENCES transaction (transaction_id)
        ON DELETE CASCADE                      -- Remove rule results if transaction is deleted
);

-- Stores the final fraud decision after combining rule results
CREATE TABLE fraud_decision (
    decision_id UUID PRIMARY KEY,             -- Unique ID for the fraud decision entry
    transaction_id VARCHAR(64) NOT NULL UNIQUE, -- Each transaction can only have one final decision
    is_fraud BOOLEAN NOT NULL,                -- TRUE = flagged as fraud, FALSE = clean
    severity INT NOT NULL,                    -- Severity or risk level (custom scoring model)
    evaluated_at TIMESTAMP NOT NULL,          -- Timestamp of when decision was made

    CONSTRAINT fk_decision_tx FOREIGN KEY (transaction_id)
        REFERENCES transaction (transaction_id)
        ON DELETE CASCADE                      -- Delete decision entry if transaction is removed
);

-- List of merchants explicitly blocked/flagged (used in blacklist rule checks)
CREATE TABLE merchant (
    id SERIAL PRIMARY KEY,
    merchant_name VARCHAR(128) NOT NULL UNIQUE,
    is_blacklisted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);


-- Configuration table for dynamic, rule-level parameters
CREATE TABLE rule_config (
    rule_name VARCHAR(128) NOT NULL,          -- Name of the rule
    config_key VARCHAR(128) NOT NULL,         -- Config parameter name
    config_value VARCHAR(128) NOT NULL,       -- Config parameter value
    PRIMARY KEY (rule_name, config_key)       -- Composite key ensures uniqueness per rule
);

-- Indexes to improve query performance for common access patterns
CREATE INDEX idx_tx_customer_id ON transaction (customer_id);     -- Fast lookup by customer
CREATE INDEX idx_tx_timestamp ON transaction (timestamp);         -- Optimize time-range queries
CREATE INDEX idx_rule_tx_id ON rule_result (transaction_id);      -- Improve join/filter by transaction
CREATE INDEX idx_decision_severity ON fraud_decision (severity);   -- Useful for severity-based reporting
