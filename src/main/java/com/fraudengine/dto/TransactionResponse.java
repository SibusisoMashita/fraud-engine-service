package com.fraudengine.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

    private String transactionId;
    private boolean isFraud;
    private int severity;
    private LocalDateTime evaluatedAt;
}
