CREATE TABLE transaction (
    transaction_id VARCHAR(64) PRIMARY KEY,
    customer_id VARCHAR(64) NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    merchant VARCHAR(128) NOT NULL,
    location VARCHAR(64),
    channel VARCHAR(32) NOT NULL
);
