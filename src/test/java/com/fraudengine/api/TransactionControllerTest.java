package com.fraudengine.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudengine.domain.Channel;
import com.fraudengine.domain.FraudDecision;
import com.fraudengine.dto.TransactionRequest;
import com.fraudengine.dto.TransactionResponse;
import com.fraudengine.mapper.TransactionMapper;
import com.fraudengine.repository.FraudDecisionRepository;
import com.fraudengine.service.FraudDecisionService;
import com.fraudengine.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private TransactionService txService;

    @MockBean
    private TransactionMapper txMapper;

    @MockBean
    private FraudDecisionRepository decisionRepo;

    @MockBean
    private FraudDecisionService fraudDecisionService;

    @Test
    void shouldProcessTransactionAndReturnDecision() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .transactionId("T1")
                .customerId("C1")
                .amount(new BigDecimal("123"))
                .timestamp(LocalDateTime.now())
                .merchant("StoreA")
                .location("Cape Town")
                .channel(Channel.MOBILE)
                .build();

        var txEntity = com.fraudengine.domain.Transaction.builder()
                .transactionId("T1")
                .build();

        FraudDecision decision = FraudDecision.builder()
                .transactionId("T1")
                .severity(3)
                .isFraud(true)
                .evaluatedAt(LocalDateTime.now())
                .build();

        when(txMapper.toEntity(any())).thenReturn(txEntity);
        doNothing().when(txService).processTransaction(txEntity);
        when(decisionRepo.findByTransactionId("T1")).thenReturn(Optional.of(decision));

        mvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("T1"))
                .andExpect(jsonPath("$.fraud").value(true))
                .andExpect(jsonPath("$.severity").value(3));
    }

    @Test
    void shouldFail_WhenDecisionNotComputed() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .transactionId("T1")
                .customerId("C1")
                .amount(new BigDecimal("100"))
                .timestamp(LocalDateTime.now())
                .merchant("StoreA")
                .location("Cape Town")
                .channel(Channel.ONLINE)
                .build();

        var txEntity = com.fraudengine.domain.Transaction.builder()
                .transactionId("T1")
                .build();

        when(txMapper.toEntity(any())).thenReturn(txEntity);
        doNothing().when(txService).processTransaction(txEntity);
        when(decisionRepo.findByTransactionId("T1")).thenReturn(Optional.empty());

        mvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
