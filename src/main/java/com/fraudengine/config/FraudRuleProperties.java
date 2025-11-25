package com.fraudengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "fraud")
public class FraudRuleProperties {

    /**
     * Rule names that are enabled via YAML.
     */
    private List<String> enabledRules = List.of();
}
