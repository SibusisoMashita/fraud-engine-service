package com.fraudengine.repository;

import com.fraudengine.domain.RuleResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RuleResultRepository extends JpaRepository<RuleResult, UUID> {

    List<RuleResult> findByTransactionId(String transactionId);
}
