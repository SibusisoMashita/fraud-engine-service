package com.fraudengine.domain;

public enum RuleName {

    HIGH_VALUE("HighValueTransactionRule"),
    VELOCITY("VelocityRule"),
    IMPOSSIBLE_TRAVEL("ImpossibleTravelRule"),
    MERCHANT_BLACKLIST("MerchantBlacklistRule"),
    OFF_HOURS("OffHoursHighRiskRule");

    private final String ruleName;

    RuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String value() {
        return ruleName;
    }

    @Override
    public String toString() {
        return ruleName;
    }
}
