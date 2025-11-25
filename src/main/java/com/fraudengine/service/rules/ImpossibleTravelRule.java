package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleName;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ImpossibleTravelRule implements FraudRule {

    private static final Logger log = LoggerFactory.getLogger(ImpossibleTravelRule.class);

    private final TransactionRepository transactionRepository;

    @Override
    public RuleResult evaluate(Transaction tx) {

        Optional<Transaction> lastTx =
                transactionRepository.findTopByCustomerIdAndTimestampBeforeOrderByTimestampDesc(tx.getCustomerId(),
                        tx.getTimestamp());

        // No previous transaction -> always safe
        if (lastTx.isEmpty()) {

            log.debug(
                    "rule_evaluation event=ImpossibleTravelRule transactionId={} customerId={} previousTxFound=false passed=true reason='No previous transaction'",
                    tx.getTransactionId(),
                    tx.getCustomerId()
            );

            return RuleResult.builder()
                    .transactionId(tx.getTransactionId())
                    .ruleName(getRuleName())
                    .passed(true)
                    .reason("No previous transaction")
                    .score(0)
                    .build();
        }

        Transaction prev = lastTx.get();
        boolean passed = prev.getLocation().equals(tx.getLocation());

        // ─────────────────────────────
        //      Structured Rule Log
        // ─────────────────────────────
        log.debug(
                "rule_evaluation event=ImpossibleTravelRule transactionId={} customerId={} prevLocation={} currLocation={} passed={}",
                tx.getTransactionId(),
                tx.getCustomerId(),
                prev.getLocation(),
                tx.getLocation(),
                passed
        );

        return RuleResult.builder()
                .transactionId(tx.getTransactionId())
                .ruleName(getRuleName())
                .passed(passed)
                .reason(passed
                        ? "Location consistent"
                        : "Location mismatch with previous transaction")
                .score(passed ? 0 : 2)
                .build();
    }

    @Override
    public String getRuleName() {
        return RuleName.IMPOSSIBLE_TRAVEL.value();
    }
}
