package com.fraudengine.service;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.service.rules.FraudRule;
import com.fraudengine.repository.RuleResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleEvaluationService {

    private final List<FraudRule> fraudRules; // Spring auto-injects all rule beans
    private final RuleResultRepository ruleResultRepository;
    private final FraudDecisionService fraudDecisionService;

    public void evaluate(Transaction transaction) {

        // Run pipeline
        List<RuleResult> results = fraudRules.stream()
                .map(rule -> rule.evaluate(transaction))
                .toList();

        // Save rule results
        ruleResultRepository.saveAll(results);

        // Build final fraud decision
        fraudDecisionService.computeDecision(transaction, results);
    }
}
