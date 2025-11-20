package com.fraudengine.domain;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuleConfigId implements Serializable {
    private String ruleName;
    private String configKey;
}
