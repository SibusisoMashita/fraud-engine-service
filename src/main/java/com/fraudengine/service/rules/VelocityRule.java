package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleName;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.TransactionRepository;
import com.fraudengine.service.RuleConfigService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class VelocityRule implements FraudRule {

    private static final Logger log = LoggerFactory.getLogger(VelocityRule.class);

    private final TransactionRepository repository;
    private final RuleConfigService configService;

    @Override
    public RuleResult evaluate(Transaction tx) {

        var cfg = configService.getConfig(getRuleName());
        int maxTxCount = Integer.parseInt(cfg.get("maxTxCount"));
        int windowMinutes = Integer.parseInt(cfg.get("windowMinutes"));

        LocalDateTime windowStart = tx.getTimestamp().minusMinutes(windowMinutes);

        Specification<Transaction> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("customerId"), tx.getCustomerId()),
                cb.greaterThanOrEqualTo(root.get("timestamp"), windowStart)
        );

        long count = repository.count(spec);

        boolean passed = count < maxTxCount;

        // ─────────────────────────────
        //   Structured Rule Audit Log
        // ─────────────────────────────
        log.debug(
                "rule_evaluation event=VelocityRule transactionId={} customerId={} count={} maxTxCount={} windowMinutes={} windowStart={} passed={}",
                tx.getTransactionId(),
                tx.getCustomerId(),
                count,
                maxTxCount,
                windowMinutes,
                windowStart,
                passed
        );

        return RuleResult.builder()
                .transactionId(tx.getTransactionId())
                .ruleName(getRuleName())
                .passed(passed)
                .reason(passed ? "Velocity normal" : "Exceeded " + maxTxCount)
                .score(passed ? 0 : 3)
                .build();
    }

    @Override
    public String getRuleName() {
        return RuleName.VELOCITY.value();
    }
}
