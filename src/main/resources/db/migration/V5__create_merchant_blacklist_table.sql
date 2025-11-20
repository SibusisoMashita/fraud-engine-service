CREATE TABLE merchant_blacklist (
    id SERIAL PRIMARY KEY,
    merchant_name VARCHAR(128) NOT NULL UNIQUE
);

-- Optional: insert sample entries
INSERT INTO merchant_blacklist (merchant_name)
VALUES ('FraudStore'),
       ('ShadyDealer'),
       ('SuspiciousMerchant');
