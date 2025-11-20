package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleName;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.service.RuleConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OffHoursHighRiskRule implements FraudRule {

    private final RuleConfigService configService;

    @Override
    public RuleResult evaluate(Transaction tx) {

        var cfg = configService.getConfig(getRuleName());
        int start = Integer.parseInt(cfg.get("startHour"));
        int end = Integer.parseInt(cfg.get("endHour"));

        int hour = tx.getTimestamp().getHour();

        boolean risky = (hour >= start && hour <= end);
        boolean passed = !risky;

        return RuleResult.builder()
                .transactionId(tx.getTransactionId())
                .ruleName(getRuleName())
                .passed(passed)
                .reason(passed ? "Normal hours" : "Risky hours: " + start + "–" + end)
                .score(passed ? 0 : 2)
                .build();
    }

    @Override
    public String getRuleName() {
        return RuleName.OFF_HOURS.value();
    }
}

