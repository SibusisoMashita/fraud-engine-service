package com.fraudengine.service;

import com.fraudengine.domain.FraudDecision;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.FraudDecisionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDecisionServiceTest {

    @Mock
    private FraudDecisionRepository repository;

    @InjectMocks
    private FraudDecisionService service;

    @Test
    void shouldComputeNonFraud_WhenAllRulesPass() {
        Transaction tx = Transaction.builder().transactionId("T1").build();

        RuleResult r1 = RuleResult.builder().passed(true).score(0).build();
        RuleResult r2 = RuleResult.builder().passed(true).score(0).build();

        service.computeDecision(tx, List.of(r1, r2));

        ArgumentCaptor<FraudDecision> captor = ArgumentCaptor.forClass(FraudDecision.class);

        verify(repository).save(captor.capture());
        FraudDecision decision = captor.getValue();

        assertEquals("T1", decision.getTransactionId());
        assertEquals(0, decision.getSeverity());
        assertFalse(decision.isFraud());
    }

    @Test
    void shouldComputeFraud_WhenAnyRuleFails() {
        Transaction tx = Transaction.builder().transactionId("T1").build();

        RuleResult r1 = RuleResult.builder().passed(false).score(3).build();
        RuleResult r2 = RuleResult.builder().passed(true).score(0).build();

        service.computeDecision(tx, List.of(r1, r2));

        ArgumentCaptor<FraudDecision> captor = ArgumentCaptor.forClass(FraudDecision.class);

        verify(repository).save(captor.capture());
        FraudDecision decision = captor.getValue();

        assertEquals(3, decision.getSeverity());
        assertTrue(decision.isFraud());
    }

    @Test
    void shouldDefaultScoreTo1_WhenNullScoreProvided() {
        Transaction tx = Transaction.builder().transactionId("T1").build();

        RuleResult failing = RuleResult.builder().passed(false).score(null).build();

        service.computeDecision(tx, List.of(failing));

        ArgumentCaptor<FraudDecision> captor = ArgumentCaptor.forClass(FraudDecision.class);

        verify(repository).save(captor.capture());
        FraudDecision decision = captor.getValue();

        assertEquals(1, decision.getSeverity());
        assertTrue(decision.isFraud());
    }
}
