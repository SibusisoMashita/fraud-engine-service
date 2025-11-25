package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;

public interface FraudRule {

    RuleResult evaluate(Transaction transaction);

    String getRuleName();

    /**
     * Builds a consistent log prefix for all logs produced by rules.
     * Ensures all rule logs start with [tx=<transactionId>].
     */
    default String prefix(Transaction transaction) {
        return "[tx=" + transaction.getTransactionId() + "]";
    }
}
