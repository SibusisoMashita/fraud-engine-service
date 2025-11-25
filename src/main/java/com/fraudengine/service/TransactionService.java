package com.fraudengine.service;

import com.fraudengine.domain.Transaction;
import com.fraudengine.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final RuleEvaluationService ruleEvaluationService;

    @Transactional
    public void processTransaction(Transaction transaction) {

        String txId = transaction.getTransactionId();

        log.info("[tx={}] 🟢 Transaction received", txId);

        // ──────────────────────────────────────────────
        //  STRUCTURED RECEIVE LOG
        // ──────────────────────────────────────────────
        log.info(
                "[tx={}] event=transaction_received customerId={} amount={} channel={} merchant={}",
                txId,
                transaction.getCustomerId(),
                transaction.getAmount(),
                transaction.getChannel(),
                transaction.getMerchant()
        );

        // Pre-check if transaction already exists
        boolean exists = transactionRepository.existsById(txId);

        log.debug(
                "[tx={}] event=transaction_existence_check exists={}",
                txId,
                exists
        );

        if (exists) {
            log.error(
                    "[tx={}] ❌ event=transaction_duplicate_detected reason='already_exists'",
                    txId
            );
            throw new IllegalStateException("Transaction already exists: " + txId);
        }

        // Persist raw transaction
        transactionRepository.save(transaction);

        log.info(
                "[tx={}] 🟢 event=transaction_persisted customerId={}",
                txId,
                transaction.getCustomerId()
        );

        // Run rule engine + fraud decision
        ruleEvaluationService.evaluate(transaction);

        log.info(
                "[tx={}] 🟣 event=transaction_processed customerId={}",
                txId,
                transaction.getCustomerId()
        );
    }

    public Transaction getTransaction(String transactionId) {

        log.debug("[tx={}] event=transaction_fetch", transactionId);

        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.error("[tx={}] ❌ event=transaction_not_found", transactionId);
                    return new IllegalArgumentException("Transaction not found: " + transactionId);
                });
    }
}
