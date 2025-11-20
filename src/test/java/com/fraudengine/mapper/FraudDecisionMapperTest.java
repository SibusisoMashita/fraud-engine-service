package com.fraudengine.mapper;

import com.fraudengine.domain.FraudDecision;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.dto.FraudDecisionResponse;
import com.fraudengine.dto.RuleResultResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDecisionMapperTest {

    @Mock
    private RuleResultMapper ruleResultMapper;

    @InjectMocks
    private FraudDecisionMapper mapper;

    @Test
    void shouldMapFraudDecisionAndRuleResults() {
        LocalDateTime now = LocalDateTime.now();

        FraudDecision decision = FraudDecision.builder()
                .transactionId("T1")
                .isFraud(true)
                .severity(4)
                .evaluatedAt(now)
                .build();

        RuleResult r1 = RuleResult.builder()
                .ruleName("HIGH_VALUE")
                .passed(false)
                .build();

        RuleResult r2 = RuleResult.builder()
                .ruleName("IMPOSSIBLE_TRAVEL")
                .passed(true)
                .build();

        RuleResultResponse resp1 = RuleResultResponse.builder()
                .ruleName("HIGH_VALUE")
                .passed(false)
                .build();

        RuleResultResponse resp2 = RuleResultResponse.builder()
                .ruleName("IMPOSSIBLE_TRAVEL")
                .passed(true)
                .build();

        when(ruleResultMapper.toResponse(r1)).thenReturn(resp1);
        when(ruleResultMapper.toResponse(r2)).thenReturn(resp2);

        FraudDecisionResponse response = mapper.toResponse(decision, List.of(r1, r2));

        assertEquals("T1", response.getTransactionId());
        assertTrue(response.isFraud());
        assertEquals(4, response.getSeverity());
        assertEquals(now, response.getEvaluatedAt());

        assertEquals(2, response.getRules().size());
        assertEquals("HIGH_VALUE", response.getRules().get(0).getRuleName());
        assertEquals("IMPOSSIBLE_TRAVEL", response.getRules().get(1).getRuleName());

        verify(ruleResultMapper).toResponse(r1);
        verify(ruleResultMapper).toResponse(r2);
    }
}
