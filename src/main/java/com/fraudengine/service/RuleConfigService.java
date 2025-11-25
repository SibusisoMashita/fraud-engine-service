package com.fraudengine.service;

import com.fraudengine.domain.RuleConfig;
import com.fraudengine.repository.RuleConfigRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleConfigService {

    private static final Logger log = LoggerFactory.getLogger(RuleConfigService.class);

    private final RuleConfigRepository repository;

    public Map<String, String> getConfig(String ruleName) {

        log.debug("event=rule_config_fetch ruleName={}", ruleName);

        var configs = repository.findByRuleName(ruleName).stream()
                .collect(Collectors.toMap(
                        RuleConfig::getConfigKey,
                        RuleConfig::getConfigValue,
                        (a, b) -> a
                ));

        log.debug(
                "event=rule_config_resolved ruleName={} configKeys={}",
                ruleName,
                configs.keySet()
        );

        return configs;
    }

    public void updateConfig(String ruleName, String key, String value) {

        log.info(
                "event=rule_config_update ruleName={} key={} value={}",
                ruleName,
                key,
                value
        );

        var config = new RuleConfig(ruleName, key, value);
        repository.save(config);

        log.debug(
                "event=rule_config_updated_persisted ruleName={} key={} value={}",
                ruleName,
                key,
                value
        );
    }
}
