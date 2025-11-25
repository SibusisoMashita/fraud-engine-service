package com.fraudengine.service;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.service.rules.FraudRule;
import com.fraudengine.repository.RuleResultRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluationService.class);

    private final List<FraudRule> fraudRules; // Spring auto-injects all rule beans
    private final RuleResultRepository ruleResultRepository;
    private final FraudDecisionService fraudDecisionService;

    public void evaluate(Transaction transaction) {

        String txId = transaction.getTransactionId();

        log.info("[tx={}] 🟢 Fraud evaluation started", txId);

        // ──────────────────────────────────────────────
        //   INFO: Begin rule evaluation pipeline
        // ──────────────────────────────────────────────
        log.info(
                "[tx={}] event=rule_evaluation_start customerId={} ruleCount={}",
                txId,
                transaction.getCustomerId(),
                fraudRules.size()
        );

        long startTime = System.currentTimeMillis();

        // Execute all fraud rules
        List<RuleResult> results = fraudRules.stream()
                .map(rule -> rule.evaluate(transaction))
                .toList();

        // Determine triggered (failed) rules
        List<String> triggered = results.stream()
                .filter(r -> !r.isPassed())
                .map(RuleResult::getRuleName)
                .collect(Collectors.toList());

        if (triggered.isEmpty()) {
            log.info("[tx={}] 🟢 All rules passed", txId);
        } else {
            log.info("[tx={}] 🟠 Rules triggered: {}", txId, String.join(", ", triggered));
        }

        // ──────────────────────────────────────────────
        //   DEBUG: Rule evaluation summary
        // ──────────────────────────────────────────────
        log.debug(
                "[tx={}] event=rule_evaluation_summary totalRules={} triggeredRules={}",
                txId,
                results.size(),
                triggered
        );

        // Persist all rule results
        ruleResultRepository.saveAll(results);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // ──────────────────────────────────────────────
        //   HUMAN-FRIENDLY END LOG
        // ──────────────────────────────────────────────
        log.info("[tx={}] 🔵 Rule evaluation completed in {} ms", txId, duration);

        // ──────────────────────────────────────────────
        //   INFO: Structured completion event
        // ──────────────────────────────────────────────
        log.info(
                "[tx={}] event=rule_evaluation_end triggeredCount={} durationMs={}",
                txId,
                triggered.size(),
                duration
        );

        // Delegate to fraud decision service
        fraudDecisionService.computeDecision(transaction, results);
    }
}
