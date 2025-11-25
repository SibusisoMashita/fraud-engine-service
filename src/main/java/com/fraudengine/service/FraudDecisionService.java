package com.fraudengine.service;

import com.fraudengine.domain.FraudDecision;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.FraudDecisionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FraudDecisionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDecisionService.class);

    private final FraudDecisionRepository fraudDecisionRepository;

    public void computeDecision(Transaction transaction, List<RuleResult> results) {

        String txId = transaction.getTransactionId();

        // Compute severity score
        int severity = results.stream()
                .filter(r -> !r.isPassed())
                .mapToInt(r -> r.getScore() != null ? r.getScore() : 1)
                .sum();

        boolean isFraud = severity > 0;

        List<String> triggeredRules = results.stream()
                .filter(r -> !r.isPassed())
                .map(RuleResult::getRuleName)
                .toList();

        LocalDateTime evaluatedAt = LocalDateTime.now();

        if (isFraud) {
            log.info("[tx={}] 🔴 Final decision: FRAUD (severity={})", txId, severity);
            log.info("[tx={}] 🟠 Rules triggered: {}", txId, triggeredRules);
        } else {
            log.info("[tx={}] 🟢 Final decision: CLEAN (severity=0)", txId);
            log.info("[tx={}] 🟢 No rules triggered", txId);
        }

        // ──────────────────────────────────────────────
        //  STRUCTURED AUDIT LOG (MACHINE READABLE)
        // ──────────────────────────────────────────────
        log.info(
                "[tx={}] event=fraud_decision customerId={} isFraud={} severity={} triggeredRules={} evaluatedAt={}",
                txId,
                transaction.getCustomerId(),
                isFraud,
                severity,
                triggeredRules,
                evaluatedAt
        );

        FraudDecision decision = FraudDecision.builder()
                .transactionId(txId)
                .severity(severity)
                .isFraud(isFraud)
                .evaluatedAt(evaluatedAt)
                .build();

        // ──────────────────────────────────────────────
        //  PERSIST DECISION + FAILURE HANDLING
        // ──────────────────────────────────────────────
        try {
            fraudDecisionRepository.save(decision);

            log.debug(
                    "[tx={}] event=fraud_decision_persisted severity={} isFraud={}",
                    txId,
                    severity,
                    isFraud
            );

        } catch (Exception e) {
            log.error(
                    "[tx={}] event=fraud_decision_persist_failed severity={} isFraud={} error={}",
                    txId,
                    severity,
                    isFraud,
                    e.getMessage(),
                    e
            );
            throw e;
        }
    }
}
