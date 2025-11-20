CREATE TABLE rule_config (
    rule_name VARCHAR(128) NOT NULL,
    config_key VARCHAR(128) NOT NULL,
    config_value VARCHAR(128) NOT NULL,
    PRIMARY KEY (rule_name, config_key)
);

INSERT INTO rule_config (rule_name, config_key, config_value) VALUES
('HighValueTransactionRule', 'threshold', '10000'),
('VelocityRule', 'maxTxCount', '3'),
('VelocityRule', 'windowMinutes', '1'),
('OffHoursHighRiskRule', 'startHour', '0'),
('OffHoursHighRiskRule', 'endHour', '4');
