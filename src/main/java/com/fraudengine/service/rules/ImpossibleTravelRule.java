package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ImpossibleTravelRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    @Override
    public RuleResult evaluate(Transaction tx) {

        Optional<Transaction> lastTx = transactionRepository.findTopByCustomerIdOrderByTimestampDesc(tx.getCustomerId());

        if (lastTx.isEmpty()) {
            return RuleResult.builder()
                    .transactionId(tx.getTransactionId())
                    .ruleName(getRuleName())
                    .passed(true)
                    .reason("No previous transaction")
                    .score(0)
                    .build();
        }

        boolean passed = lastTx.get().getLocation().equals(tx.getLocation());

        return RuleResult.builder()
                .transactionId(tx.getTransactionId())
                .ruleName(getRuleName())
                .passed(passed)
                .reason(passed ? "Location consistent" : "Location mismatch with previous transaction")
                .score(passed ? 0 : 2)
                .build();
    }

    @Override
    public String getRuleName() {
        return "ImpossibleTravelRule";
    }
}
