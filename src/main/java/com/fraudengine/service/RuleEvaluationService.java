package com.fraudengine.service;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.RuleResultRepository;
import com.fraudengine.service.rules.RuleContext;
import com.fraudengine.service.rules.RulePipeline;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluationService.class);

    private final RulePipeline rulePipeline;
    private final RuleResultRepository ruleResultRepository;
    private final FraudDecisionService fraudDecisionService;

    public void evaluate(Transaction transaction) {

        String txId = transaction.getTransactionId();

        log.info("[tx={}] 🟢 Fraud evaluation started", txId);
        log.info(
                "[tx={}] event=rule_evaluation_start customerId={}",
                txId,
                transaction.getCustomerId()
        );

        long start = System.currentTimeMillis();

        // Context passed to all rules inside the pipeline
        RuleContext ctx = RuleContext.builder()
                .startTimeMs(start)
                .build();

        // Run the pipeline (sequential rule evaluation)
        List<RuleResult> results = rulePipeline.run(transaction, ctx);

        // Identify triggered rules
        List<String> triggered = results.stream()
                .filter(r -> !r.isPassed())
                .map(RuleResult::getRuleName)
                .toList();

        if (triggered.isEmpty()) {
            log.info("[tx={}] 🟢 All rules passed", txId);
        } else {
            log.info("[tx={}] 🟠 Rules triggered: {}", txId, String.join(", ", triggered));
        }

        // Persist rule results
        ruleResultRepository.saveAll(results);

        long end = System.currentTimeMillis();
        long duration = end - start;

        log.info("[tx={}] 🔵 Rule evaluation completed in {} ms", txId, duration);

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
