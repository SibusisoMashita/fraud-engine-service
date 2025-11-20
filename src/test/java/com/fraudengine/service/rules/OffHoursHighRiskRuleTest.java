package com.fraudengine.service.rules;

import com.fraudengine.domain.Transaction;
import com.fraudengine.service.RuleConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffHoursHighRiskRuleTest {

    @Mock
    private RuleConfigService configService;

    @InjectMocks
    private OffHoursHighRiskRule rule;

    @Test
    void shouldFail_WhenHourIsRisky() {
        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .timestamp(LocalDateTime.of(2024, 1, 1, 2, 0))
                .build();

        when(configService.getConfig(rule.getRuleName()))
                .thenReturn(Map.of("startHour", "0", "endHour", "3"));

        var result = rule.evaluate(tx);

        assertFalse(result.isPassed());
        assertEquals("Risky hours: 0–3", result.getReason());
    }

    @Test
    void shouldPass_WhenHourIsNormal() {
        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .timestamp(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();

        when(configService.getConfig(rule.getRuleName()))
                .thenReturn(Map.of("startHour", "0", "endHour", "3"));

        var result = rule.evaluate(tx);

        assertTrue(result.isPassed());
        assertEquals("Normal hours", result.getReason());
    }
}
