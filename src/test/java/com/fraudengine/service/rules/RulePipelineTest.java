package com.fraudengine.service.rules;

import com.fraudengine.config.FraudRuleProperties;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RulePipelineTest {

    @Mock
    private FraudRule highValueRule;

    @Mock
    private FraudRule blacklistRule;

    @Mock
    private FraudRule velocityRule;

    @Mock
    private FraudRule impossibleTravelRule;

    @Mock
    private FraudRule offHoursRule;

    @Test
    @DisplayName("RulePipeline should run only enabled rules in the correct order and return results")
    void shouldRunOnlyEnabledRulesInOrder() {

        // -------------------------------------------------------
        // Arrange
        // -------------------------------------------------------

        Transaction tx = Transaction.builder()
                .transactionId("TX-PIPELINE-001")
                .customerId("CUST-404")
                .amount(BigDecimal.valueOf(123.45))
                .timestamp(LocalDateTime.now())
                .merchant("TestShop")
                .location("CPT")
                .channel("CARD")
                .build();

        RuleContext ctx = RuleContext.builder().startTimeMs(System.currentTimeMillis()).build();

        // Simulate enabled rules configured in application.yml
        FraudRuleProperties props = new FraudRuleProperties();
        props.setEnabledRules(List.of(
                "HIGH_VALUE",
                "MERCHANT_BLACKLIST",
                "VELOCITY"
        ));

        // Mock rule names (matching config)
        when(highValueRule.getRuleName()).thenReturn("HIGH_VALUE");
        when(blacklistRule.getRuleName()).thenReturn("MERCHANT_BLACKLIST");
        when(velocityRule.getRuleName()).thenReturn("VELOCITY");

        when(impossibleTravelRule.getRuleName()).thenReturn("IMPOSSIBLE_TRAVEL");
        when(offHoursRule.getRuleName()).thenReturn("OFF_HOURS");

        // Mock outputs
        RuleResult r1 = RuleResult.builder().ruleName("HIGH_VALUE").passed(true).build();
        RuleResult r2 = RuleResult.builder().ruleName("MERCHANT_BLACKLIST").passed(true).build();
        RuleResult r3 = RuleResult.builder().ruleName("VELOCITY").passed(false).score(3).build();

        when(highValueRule.evaluate(tx, ctx)).thenReturn(r1);
        when(blacklistRule.evaluate(tx, ctx)).thenReturn(r2);
        when(velocityRule.evaluate(tx, ctx)).thenReturn(r3);

        // Pipeline with ALL rules injected – only enabled ones should run
        RulePipeline pipeline = new RulePipeline(
                List.of(highValueRule, blacklistRule, velocityRule, impossibleTravelRule, offHoursRule),
                props
        );

        // -------------------------------------------------------
        // Act
        // -------------------------------------------------------

        List<RuleResult> results = pipeline.run(tx, ctx);

        // -------------------------------------------------------
        // Assert
        // -------------------------------------------------------

        // 1️⃣ Correct number of enabled rules executed
        assertEquals(3, results.size(), "Only HIGH_VALUE, BLACKLIST, VELOCITY should execute");

        // 2️⃣ Results returned in correct order
        assertEquals("HIGH_VALUE", results.get(0).getRuleName());
        assertEquals("MERCHANT_BLACKLIST", results.get(1).getRuleName());
        assertEquals("VELOCITY", results.get(2).getRuleName());

        // 3️⃣ Rule with failure score is correct
        assertFalse(results.get(2).isPassed());
        assertEquals(3, results.get(2).getScore());

        // 4️⃣ Disabled rules were never executed
        verify(impossibleTravelRule, never()).evaluate(any(), any());
        verify(offHoursRule, never()).evaluate(any(), any());

        // 5️⃣ Enabled rules DID execute once
        verify(highValueRule, times(1)).evaluate(tx, ctx);
        verify(blacklistRule, times(1)).evaluate(tx, ctx);
        verify(velocityRule, times(1)).evaluate(tx, ctx);
    }
}
