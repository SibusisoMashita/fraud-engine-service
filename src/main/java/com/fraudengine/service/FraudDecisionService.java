package com.fraudengine.service;

import com.fraudengine.domain.FraudDecision;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.FraudDecisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FraudDecisionService {

    private final FraudDecisionRepository fraudDecisionRepository;

    public void computeDecision(Transaction transaction, List<RuleResult> results) {

        int severity = results.stream()
                .filter(r -> !r.isPassed())
                .mapToInt(r -> r.getScore() != null ? r.getScore() : 1)
                .sum();

        boolean isFraud = severity > 0;

        FraudDecision decision = FraudDecision.builder()
                .transactionId(transaction.getTransactionId())
                .severity(severity)
                .isFraud(isFraud)
                .evaluatedAt(LocalDateTime.now())
                .build();

        fraudDecisionRepository.save(decision);
    }
}
