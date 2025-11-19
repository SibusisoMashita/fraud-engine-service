package com.fraudengine.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    @NotBlank
    private String transactionId;

    @NotBlank
    private String customerId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private LocalDateTime timestamp;

    @NotBlank
    private String merchant;

    private String location;

    @NotBlank
    private String channel;
}
