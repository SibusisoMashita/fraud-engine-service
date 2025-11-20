package com.fraudengine.service.rules;

import com.fraudengine.domain.Transaction;
import com.fraudengine.service.MerchantBlacklistService;
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
    private MerchantBlacklistService blacklistService;

    @InjectMocks
    private MerchantBlacklistRule rule;

    @Test
    void shouldPass_WhenMerchantNotBlacklisted() {
        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .merchant("Pick n Pay")
                .build();

        when(blacklistService.isBlacklisted("Pick n Pay"))
                .thenReturn(false);

        var result = rule.evaluate(tx);

        assertTrue(result.isPassed());
        assertEquals("Merchant clean", result.getReason());
    }

    @Test
    void shouldFail_WhenMerchantBlacklisted() {
        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .merchant("Scam Store")
                .build();

        when(blacklistService.isBlacklisted("Scam Store"))
                .thenReturn(true);

        var result = rule.evaluate(tx);

        assertFalse(result.isPassed());
        assertEquals("Merchant is blacklisted", result.getReason());
        assertEquals(4, result.getScore());
    }
}
