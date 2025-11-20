package com.fraudengine.mapper;

import com.fraudengine.domain.RuleResult;
import com.fraudengine.dto.RuleResultResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuleResultMapperTest {

    private final RuleResultMapper mapper = new RuleResultMapper();

    @Test
    void shouldMapEntityToResponse() {
        RuleResult entity = RuleResult.builder()
                .ruleName("HIGH_VALUE")
                .passed(false)
                .reason("Too high")
                .score(5)
                .build();

        RuleResultResponse dto = mapper.toResponse(entity);

        assertEquals("HIGH_VALUE", dto.getRuleName());
        assertFalse(dto.isPassed());
        assertEquals("Too high", dto.getReason());
        assertEquals(5, dto.getScore());
    }
}
