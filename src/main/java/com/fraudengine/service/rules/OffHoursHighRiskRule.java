package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleName;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.service.RuleConfigService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OffHoursHighRiskRule implements FraudRule {

    private static final Logger log = LoggerFactory.getLogger(OffHoursHighRiskRule.class);

    private final RuleConfigService configService;

    @Override
    public RuleResult evaluate(Transaction tx) {

        var cfg = configService.getConfig(getRuleName());
        int start = Integer.parseInt(cfg.get("startHour"));
        int end = Integer.parseInt(cfg.get("endHour"));

        int hour = tx.getTimestamp().getHour();

        boolean risky = (hour >= start && hour <= end);
        boolean passed = !risky;

        // ─────────────────────────────
        //  Structured Rule Audit Log
        // ─────────────────────────────
        log.debug(
                "rule_evaluation event=OffHoursHighRiskRule transactionId={} customerId={} hour={} startHour={} endHour={} passed={}",
                tx.getTransactionId(),
                tx.getCustomerId(),
                hour,
                start,
                end,
                passed
        );

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
