package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleName;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.TransactionRepository;
import com.fraudengine.service.RuleConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class VelocityRule implements FraudRule {

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

