package com.fraudengine.service;

import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RuleEvaluationService evaluationService;

    @InjectMocks
    private TransactionService transactionService;


    // ------------------------------------------------------------------------
    // 1️⃣ HighValueTransactionRule
    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Should process transaction that triggers HighValueTransactionRule")
    void shouldTriggerHighValueRule() {

        Transaction tx = Transaction.builder()
                .transactionId("TX-HIGHVALUE-001")
                .customerId("CUST-100")
                .amount(BigDecimal.valueOf(25000.00)) // > threshold
                .timestamp(LocalDateTime.parse("2025-01-25T10:15:00"))
                .merchant("Woolworths")
                .location("CPT")
                .channel("CARD")
                .build();

        when(transactionRepository.existsById("TX-HIGHVALUE-001"))
                .thenReturn(false);

        transactionService.processTransaction(tx);

        verify(transactionRepository).save(tx);
        verify(evaluationService).evaluate(tx);
        verifyNoMoreInteractions(transactionRepository, evaluationService);
    }


    // ------------------------------------------------------------------------
    // 2️⃣ OffHoursHighRiskRule
    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Should process transaction occurring in off-hours window (0–4 AM)")
    void shouldTriggerOffHoursRule() {

        Transaction tx = Transaction.builder()
                .transactionId("TX-OFFHOURS-001")
                .customerId("CUST-101")
                .amount(BigDecimal.valueOf(500.00))
                .timestamp(LocalDateTime.parse("2025-01-25T01:30:00")) // Off-hours
                .merchant("Checkers")
                .location("CPT")
                .channel("CARD")
                .build();

        when(transactionRepository.existsById("TX-OFFHOURS-001"))
                .thenReturn(false);

        transactionService.processTransaction(tx);

        verify(transactionRepository).save(tx);
        verify(evaluationService).evaluate(tx);
        verifyNoMoreInteractions(transactionRepository, evaluationService);
    }


    // ------------------------------------------------------------------------
    // 3️⃣ MerchantBlacklistRule
    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Should process transaction that triggers MerchantBlacklistRule")
    void shouldTriggerMerchantBlacklistRule() {

        Transaction tx = Transaction.builder()
                .transactionId("TX-BLACKLIST-001")
                .customerId("CUST-102")
                .amount(BigDecimal.valueOf(150.00))
                .timestamp(LocalDateTime.parse("2025-01-25T10:20:00"))
                .merchant("FraudStore") // blacklisted
                .location("CPT")
                .channel("CARD")
                .build();

        when(transactionRepository.existsById("TX-BLACKLIST-001"))
                .thenReturn(false);

        transactionService.processTransaction(tx);

        verify(transactionRepository).save(tx);
        verify(evaluationService).evaluate(tx);
        verifyNoMoreInteractions(transactionRepository, evaluationService);
    }


    // ------------------------------------------------------------------------
    // 4️⃣ ImpossibleTravelRule
    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Should trigger ImpossibleTravelRule when customer rapidly changes location")
    void shouldTriggerImpossibleTravelRule() {

        Transaction base = Transaction.builder()
                .transactionId("TX-TRAVEL-BASE")
                .customerId("CUST-103")
                .amount(BigDecimal.valueOf(100.00))
                .timestamp(LocalDateTime.parse("2025-01-25T09:00:00"))
                .merchant("Pick n Pay")
                .location("CPT")
                .channel("CARD")
                .build();

        Transaction tx = Transaction.builder()
                .transactionId("TX-TRAVEL-002")
                .customerId("CUST-103")
                .amount(BigDecimal.valueOf(100.00))
                .timestamp(LocalDateTime.parse("2025-01-25T09:10:00")) // +10 minutes
                .merchant("Pick n Pay")
                .location("JHB") // different city
                .channel("CARD")
                .build();

        when(transactionRepository.existsById("TX-TRAVEL-BASE")).thenReturn(false);
        when(transactionRepository.existsById("TX-TRAVEL-002")).thenReturn(false);

        // First transaction (baseline)
        transactionService.processTransaction(base);

        // Second triggers impossible travel
        transactionService.processTransaction(tx);

        verify(transactionRepository).save(base);
        verify(transactionRepository).save(tx);
        verify(evaluationService).evaluate(base);
        verify(evaluationService).evaluate(tx);

        verifyNoMoreInteractions(transactionRepository, evaluationService);
    }


    // ------------------------------------------------------------------------
    // 5️⃣ VelocityRule
    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Should trigger VelocityRule when transaction frequency exceeds limit")
    void shouldTriggerVelocityRule() {

        Transaction tx1 = Transaction.builder()
                .transactionId("TX-VEL-001")
                .customerId("CUST-104")
                .amount(BigDecimal.valueOf(50.00))
                .timestamp(LocalDateTime.parse("2025-01-25T12:00:00"))
                .merchant("Spar")
                .location("CPT")
                .channel("CARD")
                .build();

        Transaction tx2 = Transaction.builder()
                .transactionId("TX-VEL-002")
                .customerId("CUST-104")
                .amount(BigDecimal.valueOf(60.00))
                .timestamp(LocalDateTime.parse("2025-01-25T12:00:20"))
                .merchant("Spar")
                .location("CPT")
                .channel("CARD")
                .build();

        Transaction tx3 = Transaction.builder()
                .transactionId("TX-VEL-003")
                .customerId("CUST-104")
                .amount(BigDecimal.valueOf(55.00))
                .timestamp(LocalDateTime.parse("2025-01-25T12:00:40"))
                .merchant("Spar")
                .location("CPT")
                .channel("CARD")
                .build();

        Transaction tx4 = Transaction.builder()
                .transactionId("TX-VEL-004")
                .customerId("CUST-104")
                .amount(BigDecimal.valueOf(70.00))
                .timestamp(LocalDateTime.parse("2025-01-25T12:00:50"))
                .merchant("Spar")
                .location("CPT")
                .channel("CARD")
                .build();

        when(transactionRepository.existsById(anyString()))
                .thenReturn(false);

        transactionService.processTransaction(tx1);
        transactionService.processTransaction(tx2);
        transactionService.processTransaction(tx3);

        // This one should trip velocity rule
        transactionService.processTransaction(tx4);

        verify(transactionRepository).save(tx1);
        verify(transactionRepository).save(tx2);
        verify(transactionRepository).save(tx3);
        verify(transactionRepository).save(tx4);

        verify(evaluationService).evaluate(tx1);
        verify(evaluationService).evaluate(tx2);
        verify(evaluationService).evaluate(tx3);
        verify(evaluationService).evaluate(tx4);

        verifyNoMoreInteractions(transactionRepository, evaluationService);
    }


    // ------------------------------------------------------------------------
    // 6️⃣ All Rules
    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Should trigger HighValue + OffHours + Blacklist + ImpossibleTravel rules")
    void shouldTriggerAllRules() {

        Transaction base = Transaction.builder()
                .transactionId("TX-ALL-RULES-BASE")
                .customerId("CUST-999")
                .amount(BigDecimal.valueOf(500.00))
                .timestamp(LocalDateTime.parse("2025-01-25T01:00:00"))
                .merchant("Woolworths")
                .location("CPT")
                .channel("CARD")
                .build();

        Transaction tx = Transaction.builder()
                .transactionId("TX-ALL-RULES-001")
                .customerId("CUST-999")
                .amount(BigDecimal.valueOf(20000.00)) // high value
                .timestamp(LocalDateTime.parse("2025-01-25T01:45:00")) // off-hours
                .merchant("FraudStore") // blacklisted
                .location("DBN") // impossible travel
                .channel("CARD")
                .build();

        when(transactionRepository.existsById(anyString()))
                .thenReturn(false);

        // Baseline
        transactionService.processTransaction(base);

        // Full-trigger transaction
        transactionService.processTransaction(tx);

        verify(transactionRepository).save(base);
        verify(transactionRepository).save(tx);

        verify(evaluationService).evaluate(base);
        verify(evaluationService).evaluate(tx);

        verifyNoMoreInteractions(transactionRepository, evaluationService);
    }
}
