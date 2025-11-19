package com.fraudengine.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudDecisionResponse {

    private String transactionId;
    private boolean isFraud;
    private int severity;
    private LocalDateTime evaluatedAt;

    private List<RuleResultResponse> rules;
}
