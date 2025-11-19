package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import org.springframework.stereotype.Component;

@Component
public class OffHoursHighRiskRule implements FraudRule {

    private static final int START = 0;   // midnight
    private static final int END = 4;     // 4 AM

    @Override
    public RuleResult evaluate(Transaction tx) {

        int hour = tx.getTimestamp().getHour();

        boolean riskyHour = (hour >= START && hour <= END);

        boolean passed = !riskyHour;

        return RuleResult.builder()
                .transactionId(tx.getTransactionId())
                .ruleName(getRuleName())
                .passed(passed)
                .reason(passed ? "Normal hours" : "High-risk nighttime transaction")
                .score(passed ? 0 : 2)
                .build();
    }

    @Override
    public String getRuleName() {
        return "OffHoursHighRiskRule";
    }
}
