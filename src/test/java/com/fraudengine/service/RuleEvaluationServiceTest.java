package com.fraudengine.service;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.RuleResultRepository;
import com.fraudengine.service.rules.RuleContext;
import com.fraudengine.service.rules.RulePipeline;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleEvaluationServiceTest {

    @Mock
    private RulePipeline rulePipeline;

    @Mock
    private RuleResultRepository resultRepository;

    @Mock
    private FraudDecisionService decisionService;

    @InjectMocks
    private RuleEvaluationService service;

    @Test
    void shouldRunPipeline_SaveResults_AndComputeDecision() {

        Transaction tx = Transaction.builder()
                .transactionId("T1")
                .customerId("CUST-001")
                .build();

        RuleResult r1 = RuleResult.builder().ruleName("RuleA").passed(true).score(0).build();
        RuleResult r2 = RuleResult.builder().ruleName("RuleB").passed(false).score(5).build();

        List<RuleResult> results = List.of(r1, r2);

        // Mock pipeline
        when(rulePipeline.run(eq(tx), any(RuleContext.class))).thenReturn(results);

        // Execute
        service.evaluate(tx);

        // Verify rule results persisted
        verify(resultRepository).saveAll(results);

        // Verify decision computed
        verify(decisionService).computeDecision(tx, results);

        // Verify pipeline invoked correctly
        verify(rulePipeline).run(eq(tx), any(RuleContext.class));
    }
}
