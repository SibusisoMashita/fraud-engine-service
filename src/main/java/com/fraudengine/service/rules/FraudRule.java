package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;

public interface FraudRule {

    /**
     * Context-aware rule evaluation.
     * The default implementation ensures backward compatibility.
     */
    default RuleResult evaluate(Transaction transaction, RuleContext ctx) {
        return evaluate(transaction); // fallback
    }

    /**
     * Legacy single-argument evaluator.
     */
    RuleResult evaluate(Transaction transaction);

    /**
     * The name/identifier of this rule.
     */
    String getRuleName();

    /**
     * Builds a consistent log prefix for all logs produced by rules.
     * Ensures all rule logs start with [tx=<transactionId>].
     */
    default String prefix(Transaction transaction) {
        return "[tx=" + transaction.getTransactionId() + "]";
    }
}
