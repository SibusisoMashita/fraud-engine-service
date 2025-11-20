package com.fraudengine.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rule_config")
@IdClass(RuleConfigId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleConfig {

    @Id
    @Column(name = "rule_name", length = 128)
    private String ruleName;

    @Id
    @Column(name = "config_key", length = 128)
    private String configKey;

    @Column(name = "config_value", length = 128, nullable = false)
    private String configValue;
}
