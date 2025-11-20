package com.fraudengine.service.rules;

import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.TransactionRepository;
import com.fraudengine.service.RuleConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VelocityRuleTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private RuleConfigService configService;

    @InjectMocks
    private VelocityRule rule;

    @Test
    void shouldPass_WhenVelocityNormal() {
        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .customerId("C1")
                .timestamp(LocalDateTime.now())
                .build();

        when(configService.getConfig(rule.getRuleName()))
                .thenReturn(Map.of("maxTxCount", "3", "windowMinutes", "10"));

        when(repository.count(any(Specification.class)))
                .thenReturn(1L);

        var result = rule.evaluate(tx);

        assertTrue(result.isPassed());
        assertEquals("Velocity normal", result.getReason());
    }

    @Test
    void shouldFail_WhenVelocityExceeded() {
        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .customerId("C1")
                .timestamp(LocalDateTime.now())
                .build();

        when(configService.getConfig(rule.getRuleName()))
                .thenReturn(Map.of("maxTxCount", "3", "windowMinutes", "10"));

        when(repository.count(any(Specification.class)))
                .thenReturn(5L);

        var result = rule.evaluate(tx);

        assertFalse(result.isPassed());
        assertEquals("Exceeded 3", result.getReason());
        assertEquals(3, result.getScore());
    }
}
