package com.fraudengine.mapper;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.dto.RuleResultResponse;
import org.springframework.stereotype.Component;

@Component
public class RuleResultMapper {

    public RuleResultResponse toResponse(RuleResult entity) {
        return RuleResultResponse.builder()
                .ruleName(entity.getRuleName())
                .passed(entity.isPassed())
                .reason(entity.getReason())
                .score(entity.getScore())
                .build();
    }
}
