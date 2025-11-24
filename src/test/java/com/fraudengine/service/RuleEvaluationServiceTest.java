package com.fraudengine.service;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.service.rules.FraudRule;
import com.fraudengine.repository.RuleResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleEvaluationServiceTest {

    @Mock
    private RuleResultRepository resultRepository;

    @Mock
    private FraudDecisionService decisionService;

    @Mock
    private FraudRule rule1;

    @Mock
    private FraudRule rule2;

    @InjectMocks
    private RuleEvaluationService service;

    @Test
    void shouldRunRules_AndSaveResults_AndBuildDecision() {
        Transaction tx = Transaction.builder().transactionId("T1").build();

        RuleResult r1 = RuleResult.builder().passed(true).score(0).build();
        RuleResult r2 = RuleResult.builder().passed(false).score(3).build();

        when(rule1.evaluate(tx)).thenReturn(r1);
        when(rule2.evaluate(tx)).thenReturn(r2);

        // Inject rules list manually
        service = new RuleEvaluationService(List.of(rule1, rule2), resultRepository, decisionService);

        service.evaluate(tx);

        verify(resultRepository).saveAll(List.of(r1, r2));
        verify(decisionService).computeDecision(tx, List.of(r1, r2));
    }
}
