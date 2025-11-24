package com.fraudengine.service.rules;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.service.RuleConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HighValueTransactionRuleTest {

    @Mock
    private RuleConfigService configService;

    @InjectMocks
    private HighValueTransactionRule rule;

    @Test
    void shouldPass_WhenAmountBelowThreshold() {
        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .amount(new BigDecimal("500"))
                .build();

        when(configService.getConfig(rule.getRuleName()))
                .thenReturn(Map.of("threshold", "1000"));

        RuleResult result = rule.evaluate(tx);

        assertTrue(result.isPassed());
        assertEquals("Below threshold", result.getReason());
        assertEquals(0, result.getScore());
    }

    @Test
    void shouldFail_WhenAmountExceedsThreshold() {
        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .amount(new BigDecimal("1500"))
                .build();

        when(configService.getConfig(rule.getRuleName()))
                .thenReturn(Map.of("threshold", "1000"));

        RuleResult result = rule.evaluate(tx);

        assertFalse(result.isPassed());
        assertEquals("Exceeds threshold: 1000", result.getReason());
        assertEquals(5, result.getScore());
    }
}
