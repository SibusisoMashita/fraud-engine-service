package com.fraudengine.repository;

import com.fraudengine.domain.FraudDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FraudDecisionRepository extends JpaRepository<FraudDecision, UUID> {

    Optional<FraudDecision> findByTransactionId(String transactionId);
}
