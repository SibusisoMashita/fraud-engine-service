package com.fraudengine.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudengine.domain.FraudDecision;
import com.fraudengine.domain.RuleResult;
import com.fraudengine.dto.FraudDecisionResponse;
import com.fraudengine.mapper.FraudDecisionMapper;
import com.fraudengine.repository.FraudDecisionRepository;
import com.fraudengine.repository.RuleResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@WebMvcTest(FraudDecisionController.class)
class FraudDecisionControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private FraudDecisionRepository fraudDecisionRepository;

    @MockBean
    private RuleResultRepository ruleResultRepository;

    @MockBean
    private FraudDecisionMapper fraudDecisionMapper;

    @Autowired
    private ObjectMapper mapper;

    // -------------------------------------------
    // 1️⃣ GET /fraud/{transactionId} success
    // -------------------------------------------
    @Test
    void shouldReturnDecision() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        FraudDecision fd = FraudDecision.builder()
                .transactionId("T1")
                .isFraud(true)
                .severity(5)
                .evaluatedAt(now)
                .build();

        RuleResult rr = RuleResult.builder().transactionId("T1").ruleName("RULE").passed(false).build();

        FraudDecisionResponse response = FraudDecisionResponse.builder()
                .transactionId("T1").severity(5).isFraud(true).evaluatedAt(now).rules(List.of())
                .build();

        when(fraudDecisionRepository.findByTransactionId("T1")).thenReturn(Optional.of(fd));
        when(ruleResultRepository.findByTransactionId("T1")).thenReturn(List.of(rr));
        when(fraudDecisionMapper.toResponse(fd, List.of(rr))).thenReturn(response);

        mvc.perform(get("/api/v1/fraud/T1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("T1"))
                .andExpect(jsonPath("$.severity").value(5))
                .andExpect(jsonPath("$.fraud").value(true));
    }


    // -------------------------------------------
    // 2️⃣ GET /fraud/{transactionId} NOT FOUND
    // -------------------------------------------
    @Test
    void shouldReturnNotFoundWhenMissing() throws Exception {
        when(fraudDecisionRepository.findByTransactionId("T1"))
                .thenReturn(Optional.empty());

        mvc.perform(get("/api/v1/fraud/T1"))
                .andExpect(status().isNotFound());
    }


    // -------------------------------------------
    // 3️⃣ listFlagged – no filters (baseline)
    // -------------------------------------------
    @Test
    void listFlagged_noFilters() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        FraudDecision fd = FraudDecision.builder()
                .transactionId("T1").isFraud(true).severity(3).evaluatedAt(now)
                .build();

        FraudDecisionResponse dto = FraudDecisionResponse.builder()
                .transactionId("T1").severity(3).isFraud(true).evaluatedAt(now).rules(List.of())
                .build();

        when(fraudDecisionRepository.findAll()).thenReturn(List.of(fd));
        when(ruleResultRepository.findByTransactionId("T1")).thenReturn(List.of());
        when(fraudDecisionMapper.toResponse(fd, List.of())).thenReturn(dto);

        mvc.perform(get("/api/v1/fraud/flagged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value("T1"));
    }


    // -------------------------------------------
    // 4️⃣ severity filter: EXCLUDED branch (d.getSeverity() < severity)
    // -------------------------------------------
    @Test
    void listFlagged_shouldExcludeWhenSeverityTooLow() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        // severity = 2 < required 5
        FraudDecision fd = FraudDecision.builder()
                .transactionId("T1").isFraud(true).severity(2).evaluatedAt(now)
                .build();

        when(fraudDecisionRepository.findAll()).thenReturn(List.of(fd));
        when(ruleResultRepository.findByTransactionId("T1")).thenReturn(List.of());
        when(fraudDecisionMapper.toResponse(any(), any()))
                .thenReturn(FraudDecisionResponse.builder().transactionId("T1").build());

        mvc.perform(get("/api/v1/fraud/flagged")
                .param("severity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0)); // excluded
    }


    // -------------------------------------------
    // 5️⃣ fromDate filter: EXCLUDED (evaluatedAt < fromDate)
    // -------------------------------------------
    @Test
    void listFlagged_shouldExcludeWhenBeforeFromDate() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(1);

        FraudDecision fd = FraudDecision.builder()
                .transactionId("T1").isFraud(true).severity(5).evaluatedAt(now) // before future
                .build();

        when(fraudDecisionRepository.findAll()).thenReturn(List.of(fd));
        when(ruleResultRepository.findByTransactionId("T1")).thenReturn(List.of());
        when(fraudDecisionMapper.toResponse(any(), any()))
                .thenReturn(FraudDecisionResponse.builder().transactionId("T1").build());

        mvc.perform(get("/api/v1/fraud/flagged")
                .param("fromDate", future.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0)); // excluded
    }


    // -------------------------------------------
    // 6️⃣ toDate filter: EXCLUDED (evaluatedAt > toDate)
    // -------------------------------------------
    @Test
    void listFlagged_shouldExcludeWhenAfterToDate() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusDays(1);

        FraudDecision fd = FraudDecision.builder()
                .transactionId("T1").isFraud(true).severity(5).evaluatedAt(now) // after past
                .build();

        when(fraudDecisionRepository.findAll()).thenReturn(List.of(fd));
        when(ruleResultRepository.findByTransactionId("T1")).thenReturn(List.of());
        when(fraudDecisionMapper.toResponse(any(), any()))
                .thenReturn(FraudDecisionResponse.builder().transactionId("T1").build());

        mvc.perform(get("/api/v1/fraud/flagged")
                .param("toDate", past.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0)); // excluded
    }
}
