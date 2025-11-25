package com.fraudengine.service.rules;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RuleContext {
    private long startTimeMs;
    private Object metadata; // expandable
}
