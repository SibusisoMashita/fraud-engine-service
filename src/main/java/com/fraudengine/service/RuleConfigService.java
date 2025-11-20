package com.fraudengine.service;

import com.fraudengine.domain.RuleConfig;
import com.fraudengine.repository.RuleConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleConfigService {

    private final RuleConfigRepository repository;

    public Map<String, String> getConfig(String ruleName) {
        return repository.findByRuleName(ruleName).stream()
                .collect(Collectors.toMap(
                        RuleConfig::getConfigKey,
                        RuleConfig::getConfigValue,
                        (a, b) -> a
                ));
    }

    public void updateConfig(String ruleName, String key, String value) {
        var config = new RuleConfig(ruleName, key, value);
        repository.save(config);
    }
}
