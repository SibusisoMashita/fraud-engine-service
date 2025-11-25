package com.fraudengine.config;

import com.fraudengine.service.rules.FraudRule;
import com.fraudengine.service.rules.RulePipeline;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RuleEngineConfig {

    private final FraudRuleProperties fraudRuleProperties;

    @Bean
    public RulePipeline rulePipeline(List<FraudRule> fraudRules) {
        return new RulePipeline(fraudRules, fraudRuleProperties);
    }
}
