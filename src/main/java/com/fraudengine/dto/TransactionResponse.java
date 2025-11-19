package com.fraudengine.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private String transactionId;
    private boolean isFraud;
    private int severity;
    private LocalDateTime evaluatedAt;
}
