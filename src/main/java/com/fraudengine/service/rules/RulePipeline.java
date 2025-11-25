package com.fraudengine.service.rules;

import com.fraudengine.config.FraudRuleProperties;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class RulePipeline {

    private final List<FraudRule> rules;
    private final FraudRuleProperties ruleProperties;

    public List<RuleResult> run(Transaction tx, RuleContext ctx) {

        List<String> enabled = ruleProperties.getEnabledRules();

        List<FraudRule> activeRules = rules.stream()
                .filter(r -> enabled.contains(r.getRuleName()))
                .toList();

        log.info("[tx={}] 🧩 Active rules = {}", 
                tx.getTransactionId(), 
                activeRules.stream().map(FraudRule::getRuleName).toList()
        );

        List<RuleResult> results = new ArrayList<>();

        for (FraudRule rule : activeRules) {
            RuleResult result = rule.evaluate(tx, ctx);
            results.add(result);
        }

        return results;
    }
}
