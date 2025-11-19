package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class HighValueTransactionRule implements FraudRule {

    private static final BigDecimal THRESHOLD = new BigDecimal("10000");

    @Override
    public RuleResult evaluate(Transaction tx) {

        boolean passed = tx.getAmount().compareTo(THRESHOLD) <= 0;

        return RuleResult.builder()
                .transactionId(tx.getTransactionId())
                .ruleName(getRuleName())
                .passed(passed)
                .reason(passed ? "Below threshold" : "Amount exceeds R10,000")
                .score(passed ? 0 : 5)
                .build();
    }

    @Override
    public String getRuleName() {
        return "HighValueTransactionRule";
    }
}
