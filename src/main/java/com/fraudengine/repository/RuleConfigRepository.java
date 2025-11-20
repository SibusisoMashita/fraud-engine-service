package com.fraudengine.repository;

import com.fraudengine.domain.RuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleConfigRepository extends JpaRepository<RuleConfig, String> {
    List<RuleConfig> findByRuleName(String ruleName);
}
