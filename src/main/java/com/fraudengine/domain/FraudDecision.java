package com.fraudengine.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_decision")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudDecision {

    @Id
    @GeneratedValue
    @Column(name = "decision_id", nullable = false)
    private UUID decisionId;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 64)
    private String transactionId;

    @Column(name = "is_fraud", nullable = false)
    private boolean isFraud;

    @Column(nullable = false)
    private Integer severity;

    @Column(name = "evaluated_at", nullable = false)
    private LocalDateTime evaluatedAt;
}
