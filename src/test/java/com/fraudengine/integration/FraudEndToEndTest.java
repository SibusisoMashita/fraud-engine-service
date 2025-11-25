package com.fraudengine.integration;

import com.fraudengine.containers.PostgresContainerConfig;
import com.fraudengine.domain.FraudDecision;
import com.fraudengine.domain.RuleResult;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class FraudEndToEndTest extends PostgresContainerConfig {

    @Autowired
    MockMvc mvc;

    @Autowired
    RuleResultRepository ruleResultRepository;

    @Autowired
    FraudDecisionRepository fraudDecisionRepository;


    @Test
    void debugClasspath() {
        System.out.println("Classpath Resources:");
        System.out.println(getClass().getClassLoader().getResource("testcontainers.properties"));
    }


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
        List<RuleResult> results = ruleResultRepository.findByTransactionId("TX-HV-E2E");
        FraudDecision decision = fraudDecisionRepository.findByTransactionId("TX-HV-E2E").orElseThrow();

        // Step 3: Assert rule engine behavior
        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(r -> r.getRuleName().equals("HIGH_VALUE") && !r.isPassed());

        // Step 4: Assert final fraud decision
        assertThat(decision.isFraud()).isTrue();
        assertThat(decision.getSeverity()).isGreaterThan(0);
    }
}
