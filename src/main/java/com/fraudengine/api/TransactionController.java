package com.fraudengine.api;

import com.fraudengine.dto.TransactionRequest;
import com.fraudengine.dto.TransactionResponse;
import com.fraudengine.mapper.TransactionMapper;
import com.fraudengine.service.FraudDecisionService;
import com.fraudengine.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;
    private final FraudDecisionService fraudDecisionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> processTransaction(
            @Valid @RequestBody TransactionRequest request) {

        var tx = transactionMapper.toEntity(request);

        transactionService.processTransaction(tx);

        // Retrieve the final decision (saved by rule engine)
        var decisions = fraudDecisionService.findByTransactionId(tx.getTransactionId());

        if (decisions.isEmpty()) {
            return ResponseEntity.ok(
                    TransactionResponse.builder()
                            .transactionId(tx.getTransactionId())
                            .isFraud(false)
                            .severity(0)
                            .evaluatedAt(null)
                            .build()
            );
        }

        var decision = decisions.get();

        return ResponseEntity.ok(
                TransactionResponse.builder()
                        .transactionId(decision.getTransactionId())
                        .isFraud(decision.isFraud())
                        .severity(decision.getSeverity())
                        .evaluatedAt(decision.getEvaluatedAt())
                        .build()
        );
    }
}
