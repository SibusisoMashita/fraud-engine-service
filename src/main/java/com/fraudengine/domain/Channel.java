package com.fraudengine.domain;

public enum Channel {
    CARD,
    EFT,
    ATM,
    POS,
    ONLINE,
    MOBILE,
    USSD,
    BRANCH,
    DEBIT_ORDER,
    QR_CODE,
    TAP_TO_PAY,
    WALLET,
    UNKNOWN;

    public static Channel fromString(String value) {
        try {
            return Channel.valueOf(value.toUpperCase());
        } catch (Exception e) {
            return UNKNOWN; // Safe fallback
        }
    }
}
