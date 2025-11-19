package com.fraudengine.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "rule_result")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleResult {

    @Id
    @GeneratedValue
    @Column(name = "result_id", nullable = false)
    private UUID resultId;

    @Column(name = "transaction_id", nullable = false, length = 64)
    private String transactionId;

    @Column(name = "rule_name", nullable = false, length = 128)
    private String ruleName;

    @Column(nullable = false)
    private boolean passed;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column
    private Integer score;
}
