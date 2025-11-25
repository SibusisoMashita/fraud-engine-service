package com.fraudengine.service.rules;

import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImpossibleTravelRuleTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private ImpossibleTravelRule rule;

    @Test
    void shouldPass_WhenNoPreviousTransaction() {
        LocalDateTime now = LocalDateTime.now();
        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .customerId("C1")
                .timestamp(now)
                .location("Cape Town")
                .amount(BigDecimal.ONE)
                .merchant("StoreA")
                .channel("WEB")
                .build();

        when(repository.findTopByCustomerIdAndTimestampBeforeOrderByTimestampDesc("C1", now))
                .thenReturn(Optional.empty());

        var result = rule.evaluate(tx);

        assertTrue(result.isPassed());
        assertEquals("No previous transaction", result.getReason());
    }

    @Test
    void shouldPass_WhenLocationMatches() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime prevTime = now.minusMinutes(30);
        Transaction lastTx = Transaction.builder()
                .transactionId("T0")
                .customerId("C1")
                .timestamp(prevTime)
                .location("Cape Town")
                .amount(BigDecimal.ONE)
                .merchant("StoreA")
                .channel("WEB")
                .build();

        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .customerId("C1")
                .timestamp(now)
                .location("Cape Town")
                .amount(BigDecimal.ONE)
                .merchant("StoreA")
                .channel("WEB")
                .build();

        when(repository.findTopByCustomerIdAndTimestampBeforeOrderByTimestampDesc("C1", now))
                .thenReturn(Optional.of(lastTx));

        var result = rule.evaluate(tx);

        assertTrue(result.isPassed());
        assertEquals("Location consistent", result.getReason());
    }

    @Test
    void shouldFail_WhenLocationMismatch() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime prevTime = now.minusMinutes(10);
        Transaction lastTx = Transaction.builder()
                .transactionId("T0")
                .customerId("C1")
                .timestamp(prevTime)
                .location("Cape Town")
                .amount(BigDecimal.ONE)
                .merchant("StoreA")
                .channel("WEB")
                .build();

        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .customerId("C1")
                .timestamp(now)
                .location("Joburg")
                .amount(BigDecimal.ONE)
                .merchant("StoreA")
                .channel("WEB")
                .build();

        when(repository.findTopByCustomerIdAndTimestampBeforeOrderByTimestampDesc("C1", now))
                .thenReturn(Optional.of(lastTx));

        var result = rule.evaluate(tx);

        assertFalse(result.isPassed());
        assertEquals("Location mismatch with previous transaction", result.getReason());
    }
}
