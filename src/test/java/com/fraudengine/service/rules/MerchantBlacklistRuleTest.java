package com.fraudengine.service.rules;

import com.fraudengine.domain.Transaction;
import com.fraudengine.service.MerchantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantBlacklistRuleTest {

    @Mock
    private MerchantService merchantService;

    @InjectMocks
    private MerchantBlacklistRule rule;

    @Test
    void shouldPass_WhenMerchantNotBlacklisted() {
        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .merchant("Pick n Pay")
                .build();

        when(merchantService.isBlacklisted("Pick n Pay"))
                .thenReturn(false);

        var result = rule.evaluate(tx);

        assertTrue(result.isPassed());
        assertEquals("Merchant clean or not registered", result.getReason());
    }

    @Test
    void shouldFail_WhenMerchantBlacklisted() {
        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .merchant("Scam Store")
                .build();

        when(merchantService.isBlacklisted("Scam Store"))
                .thenReturn(true);

        var result = rule.evaluate(tx);

        assertFalse(result.isPassed());
        assertEquals("Merchant is blacklisted", result.getReason());
        assertEquals(4, result.getScore());
    }

    @Test
    void shouldPass_WhenMerchantNotInRegistry() {
        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .merchant("New Unknown Shop")
                .build();

        // New behavior: if merchant isn't found, service throws
        when(merchantService.isBlacklisted("New Unknown Shop"))
                .thenThrow(new IllegalArgumentException("Merchant not found"));

        var result = rule.evaluate(tx);

        // Rule treats unknown merchant as clean
        assertTrue(result.isPassed());
        assertEquals("Merchant clean or not registered", result.getReason());
    }
}
