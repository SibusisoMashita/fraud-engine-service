package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class VelocityRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    private static final int MAX_TX_COUNT = 3;
    private static final int WINDOW_MINUTES = 1;

    @Override
    public RuleResult evaluate(Transaction tx) {

        LocalDateTime windowStart = tx.getTimestamp().minusMinutes(WINDOW_MINUTES);

        long count = transactionRepository.count((root, query, cb) ->
                cb.and(
                        cb.equal(root.get("customerId"), tx.getCustomerId()),
                        cb.greaterThanOrEqualTo(root.get("timestamp"), windowStart)
                )
        );

        boolean passed = count < MAX_TX_COUNT;

        return RuleResult.builder()
                .transactionId(tx.getTransactionId())
                .ruleName(getRuleName())
                .passed(passed)
                .reason(passed ? "Velocity normal" : "Too many transactions in 1-minute window")
                .score(passed ? 0 : 3)
                .build();
    }

    @Override
    public String getRuleName() {
        return "VelocityRule";
    }
}
