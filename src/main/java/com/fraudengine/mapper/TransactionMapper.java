package com.fraudengine.mapper;

import com.fraudengine.domain.Transaction;
import com.fraudengine.dto.TransactionRequest;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionRequest dto) {
        return Transaction.builder()
                .transactionId(dto.getTransactionId())
                .customerId(dto.getCustomerId())
                .amount(dto.getAmount())
                .timestamp(dto.getTimestamp())
                .merchant(dto.getMerchant())
                .location(dto.getLocation())
                .channel(dto.getChannel().name())
                .build();
    }
}
