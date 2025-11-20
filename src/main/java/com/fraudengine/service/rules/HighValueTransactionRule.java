package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleName;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.service.RuleConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class HighValueTransactionRule implements FraudRule {

    private final RuleConfigService configService;

    @Override
    public RuleResult evaluate(Transaction tx) {

        var cfg = configService.getConfig(getRuleName());
        BigDecimal threshold = new BigDecimal(cfg.get("threshold"));

        boolean passed = tx.getAmount().compareTo(threshold) <= 0;

        return RuleResult.builder()
                .transactionId(tx.getTransactionId())
                .ruleName(getRuleName())
                .passed(passed)
                .reason(passed ? "Below threshold" : "Exceeds threshold: " + threshold)
                .score(passed ? 0 : 5)
                .build();
    }

    @Override
    public String getRuleName() {
        return RuleName.HIGH_VALUE.value();
    }
}

