package com.fraudengine.mapper;

import com.fraudengine.domain.FraudDecision;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.dto.FraudDecisionResponse;
import com.fraudengine.dto.RuleResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FraudDecisionMapper {

    private final RuleResultMapper ruleResultMapper;

    public FraudDecisionResponse toResponse(FraudDecision entity, List<RuleResult> ruleResults) {

        List<RuleResultResponse> rules = ruleResults.stream()
                .map(ruleResultMapper::toResponse)
                .toList();

        return FraudDecisionResponse.builder()
                .transactionId(entity.getTransactionId())
                .isFraud(entity.isFraud())
                .severity(entity.getSeverity())
                .evaluatedAt(entity.getEvaluatedAt())
                .rules(rules)
                .build();
    }
}
