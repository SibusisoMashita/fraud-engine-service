package com.fraudengine.repository;

import com.fraudengine.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, String>,
        JpaSpecificationExecutor<Transaction> {
    Optional<Transaction> findTopByCustomerIdOrderByTimestampDesc(String customerId);
}

