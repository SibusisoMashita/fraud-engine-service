package com.fraudengine.service.rules;

import com.fraudengine.config.FraudRuleProperties;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class RulePipeline {

    private final List<FraudRule> rules;
    private final FraudRuleProperties ruleProperties;

    public List<RuleResult> run(Transaction tx, RuleContext ctx) {

        List<String> enabled = ruleProperties.getEnabledRules();

        // Normalize enabled list: support enum-style names mapping to actual rule bean names
        List<String> normalized = enabled.stream()
                .map(s -> switch (s) {
                    case "HIGH_VALUE" -> "HighValueTransactionRule";
                    case "MERCHANT_BLACKLIST" -> "MerchantBlacklistRule";
                    case "VELOCITY" -> "VelocityRule";
                    case "IMPOSSIBLE_TRAVEL" -> "ImpossibleTravelRule";
                    case "OFF_HOURS" -> "OffHoursHighRiskRule";
                    default -> s;
                })
                .collect(Collectors.toList());

        List<FraudRule> activeRules = rules.stream()
                .filter(r -> normalized.contains(r.getRuleName()))
                .toList();

        log.info("[tx={}] \ud83e\udde9 Active rules = {}",
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
