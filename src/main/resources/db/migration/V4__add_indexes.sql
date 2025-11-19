CREATE INDEX idx_tx_customer_id ON transaction (customer_id);
CREATE INDEX idx_tx_timestamp ON transaction (timestamp);
CREATE INDEX idx_rule_tx_id ON rule_result (transaction_id);
CREATE INDEX idx_decision_severity ON fraud_decision (severity);
