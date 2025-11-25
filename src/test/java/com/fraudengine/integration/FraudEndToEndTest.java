package com.fraudengine.integration;

import com.fraudengine.domain.FraudDecision;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.domain.RuleName;
import com.fraudengine.repository.FraudDecisionRepository;
import com.fraudengine.repository.RuleResultRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class FraudEndToEndTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    RuleResultRepository ruleResultRepository;

    @Autowired
    FraudDecisionRepository fraudDecisionRepository;

    @Test
    @DisplayName("E2E: Should process high-value transaction and record rule results + decision")
    void fullHighValueFlow() throws Exception {

        String json = """
                {
                  "transactionId": "TX-HV-E2E",
                  "customerId": "CUST-200",
                  "amount": 25000.00,
                  "timestamp": "2025-01-25T10:15:00",
                  "merchant": "Woolworths",
                  "location": "CPT",
                  "channel": "CARD"
                }
                """;

        // Step 1: Hit the real API
        mvc.perform(
                post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isOk());

        // Step 2: Database assertions (REAL DB!)
        List<RuleResult> results = waitForRuleResults("TX-HV-E2E");
        FraudDecision decision = fraudDecisionRepository.findByTransactionId("TX-HV-E2E").orElseThrow();

        // Step 3: Assert rule engine behavior
        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(r -> r.getRuleName().equals(RuleName.HIGH_VALUE.value()) && !r.isPassed());

        // Step 4: Assert final fraud decision
        assertThat(decision.isFraud()).isTrue();
        assertThat(decision.getSeverity()).isGreaterThan(0);
    }

    private List<RuleResult> waitForRuleResults(String txId) {
        Instant start = Instant.now();
        List<RuleResult> results;
        do {
            results = ruleResultRepository.findByTransactionId(txId);
            if (!results.isEmpty()) return results;
            try { Thread.sleep(50); } catch (InterruptedException ignored) { }
        } while (Duration.between(start, Instant.now()).toMillis() < 2000); // wait up to 2s
        return results; // may be empty
    }
}
