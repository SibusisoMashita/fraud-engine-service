package com.fraudengine.api;

import com.fraudengine.dto.FraudDecisionResponse;
import com.fraudengine.mapper.FraudDecisionMapper;
import com.fraudengine.repository.FraudDecisionRepository;
import com.fraudengine.repository.RuleResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
public class FraudDecisionController {

    private final FraudDecisionRepository fraudDecisionRepository;
    private final RuleResultRepository ruleResultRepository;
    private final FraudDecisionMapper fraudDecisionMapper;

    @GetMapping("/{transactionId}")
    public ResponseEntity<FraudDecisionResponse> getDecision(
            @PathVariable String transactionId) {

        var decision = fraudDecisionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        var results = ruleResultRepository.findByTransactionId(transactionId);

        return ResponseEntity.ok(
                fraudDecisionMapper.toResponse(decision, results)
        );
    }

    @GetMapping("/flagged")
    public ResponseEntity<List<FraudDecisionResponse>> listFlagged(
            @RequestParam(required = false) Integer severity,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate) {

        var all = fraudDecisionRepository.findAll();

        var filtered = all.stream()
                .filter(decision -> decision.isFraud())
                .filter(d -> severity == null || d.getSeverity() >= severity)
                .filter(d -> fromDate == null || !d.getEvaluatedAt().isBefore(fromDate))
                .filter(d -> toDate == null || !d.getEvaluatedAt().isAfter(toDate))
                .map(d -> fraudDecisionMapper.toResponse(
                        d,
                        ruleResultRepository.findByTransactionId(d.getTransactionId())
                ))
                .toList();

        return ResponseEntity.ok(filtered);
    }
}
