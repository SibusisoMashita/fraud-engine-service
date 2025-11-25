-- Seed blacklist with known high-risk or banned merchants
INSERT INTO merchant (merchant_name, is_blacklisted)
VALUES
    ('FraudStore', TRUE),
    ('ShadyDealer', TRUE),
    ('SuspiciousMerchant', TRUE),
    ('GhostElectronics', TRUE),
    ('DarkWebMart', TRUE),
    ('PhantomTraders', TRUE),
    ('SketchyImports', TRUE),
    ('CryptoScamHub', TRUE),
    ('FakeSneakerPalace', TRUE),
    ('ShadowMarket', TRUE),
    ('UndergroundOutlet', TRUE),
    ('BlackHatSupplies', TRUE),
    ('UntrustedPayments', TRUE),
    ('CardTestMerchant', TRUE),
    ('ScamWorld', TRUE),
    ('StolenGoodsDepot', TRUE),
    ('FraudulentServicesInc', TRUE),

    -- Neutral / unknown merchants (default not blacklisted)
    ('MerchantX', FALSE),
    ('MerchantY', FALSE),
    ('MerchantZ', FALSE),
    ('StoreAlpha', FALSE),
    ('StoreBeta', FALSE),
    ('StoreGamma', FALSE),
    ('UnknownCoffeeShop', FALSE),
    ('RandomElectronics', FALSE),
    ('ClothingOutletZA', FALSE),
    ('TechStore123', FALSE),
    ('OnlineMallTest', FALSE),
    ('GenericMerchant01', FALSE),
    ('GenericMerchant02', FALSE),
    ('GenericMerchant03', FALSE),
    ('SmallTownMart', FALSE),
    ('BudgetBazaar', FALSE),

    -- Realistic SA retail brands (safe, legitimate)
    ('Woolworths', FALSE),
    ('Checkers', FALSE),
    ('Shoprite', FALSE),
    ('Pick n Pay', FALSE),
    ('Clicks', FALSE),
    ('Dis-Chem', FALSE),
    ('Makro', FALSE),
    ('Game', FALSE),
    ('Builders Warehouse', FALSE),
    ('Takealot', FALSE),
    ('Spar', FALSE),
    ('Ackermans', FALSE),
    ('Pep Stores', FALSE),
    ('Mr Price', FALSE),
    ('Foschini', FALSE),
    ('Cape Union Mart', FALSE),
    ('TotalSports', FALSE),
    ('Vodacom Shop', FALSE),
    ('MTN Store', FALSE),
    ('Cell C Shop', FALSE),

    -- Ecommerce
    ('Superbalist', FALSE),
    ('OneDayOnly', FALSE),
    ('iStore', FALSE),
    ('Incredible Connection', FALSE),
    ('HiFi Corp', FALSE),
    ('GadgetsOnline', FALSE),
    ('MegaDiscounts', FALSE),
    ('DailyDealsMart', FALSE);



-- Seed default rule configuration values for the rule engine
INSERT INTO rule_config (rule_name, config_key, config_value) VALUES
    -- Flags transactions exceeding this amount as high risk
    ('HighValueTransactionRule', 'threshold', '10000'),

    -- Velocity rule: max allowed number of transactions within the time window
    ('VelocityRule', 'maxTxCount', '3'),
    -- Time window (in minutes) for velocity evaluation
    ('VelocityRule', 'windowMinutes', '1'),

    -- Off-hours rule: high-risk time window (00:00 to 04:00)
    ('OffHoursHighRiskRule', 'startHour', '0'),
    ('OffHoursHighRiskRule', 'endHour', '4');
