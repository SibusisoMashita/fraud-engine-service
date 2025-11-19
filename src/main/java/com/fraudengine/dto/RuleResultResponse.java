package com.fraudengine.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleResultResponse {

    private String ruleName;
    private boolean passed;
    private String reason;
    private Integer score;
}
