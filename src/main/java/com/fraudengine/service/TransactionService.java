package com.fraudengine.service;

import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final RuleEvaluationService ruleEvaluationService;

    @Transactional
    public void processTransaction(Transaction transaction) {

        // Check if transaction exists already
        if (transactionRepository.existsById(transaction.getTransactionId())) {
            throw new IllegalStateException("Transaction already exists: " + transaction.getTransactionId());
        }

        // Store raw transaction first (immutable audit record)
        transactionRepository.save(transaction);

        // Run rule engine + decision logic
        ruleEvaluationService.evaluate(transaction);
    }

    public Transaction getTransaction(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
    }
}
