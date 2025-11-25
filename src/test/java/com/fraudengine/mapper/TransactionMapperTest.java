package com.fraudengine.mapper;

import com.fraudengine.domain.Channel;
import com.fraudengine.domain.Transaction;
import com.fraudengine.dto.TransactionRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {

    private final TransactionMapper mapper = new TransactionMapper();

    @Test
    void shouldMapRequestToEntity() {
        LocalDateTime now = LocalDateTime.now();

        TransactionRequest dto = TransactionRequest.builder()
                .transactionId("T1")
                .customerId("C1")
                .amount(new BigDecimal("123.45"))
                .timestamp(now)
                .merchant("ShopA")
                .location("Cape Town")
                .channel(Channel.ONLINE)
                .build();

        Transaction entity = mapper.toEntity(dto);

        assertEquals("T1", entity.getTransactionId());
        assertEquals("C1", entity.getCustomerId());
        assertEquals(new BigDecimal("123.45"), entity.getAmount());
        assertEquals(now, entity.getTimestamp());
        assertEquals("ShopA", entity.getMerchant());
        assertEquals("Cape Town", entity.getLocation());
        assertEquals(Channel.ONLINE.name(), entity.getChannel());
    }
}
