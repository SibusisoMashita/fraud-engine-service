package com.fraudengine.service.rules;

import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .customerId("C1")
                .build();

        when(repository.findTopByCustomerIdOrderByTimestampDesc("C1"))
                .thenReturn(Optional.empty());

        var result = rule.evaluate(tx);

        assertTrue(result.isPassed());
        assertEquals("No previous transaction", result.getReason());
    }

    @Test
    void shouldPass_WhenLocationMatches() {
        Transaction lastTx = Transaction.builder()
                .location("Cape Town")
                .build();

        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .customerId("C1")
                .location("Cape Town")
                .build();

        when(repository.findTopByCustomerIdOrderByTimestampDesc("C1"))
                .thenReturn(Optional.of(lastTx));

        var result = rule.evaluate(tx);

        assertTrue(result.isPassed());
        assertEquals("Location consistent", result.getReason());
    }

    @Test
    void shouldFail_WhenLocationMismatch() {
        Transaction lastTx = Transaction.builder()
                .location("Cape Town")
                .build();

        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .customerId("C1")
                .location("Joburg")
                .build();

        when(repository.findTopByCustomerIdOrderByTimestampDesc("C1"))
                .thenReturn(Optional.of(lastTx));

        var result = rule.evaluate(tx);

        assertFalse(result.isPassed());
        assertEquals("Location mismatch with previous transaction", result.getReason());
    }
}
