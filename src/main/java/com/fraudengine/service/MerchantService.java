package com.fraudengine.service;

import com.fraudengine.domain.Merchant;
import com.fraudengine.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private static final Logger log = LoggerFactory.getLogger(MerchantService.class);

    private final MerchantRepository repository;

    // ----------------------------------------------------------------------
    // Fetch all merchants (blacklisted or not)
    // ----------------------------------------------------------------------
    public List<Merchant> getAll() {
        log.debug("[merchant=ALL] event=merchant_fetch_all");
        return repository.findAll();
    }

    // ----------------------------------------------------------------------
    // Create a new merchant
    // ----------------------------------------------------------------------
    public Merchant createMerchant(String merchantName) {

        log.debug("[merchant={}] event=merchant_create_check exists={}",
                merchantName,
                repository.existsByMerchantName(merchantName)
        );

        if (repository.existsByMerchantName(merchantName)) {
            log.error("[merchant={}] ❌ event=merchant_create_failed reason='already_exists'", merchantName);
            throw new IllegalStateException("Merchant already exists: " + merchantName);
        }

        Merchant merchant = Merchant.builder()
                .merchantName(merchantName)
                .isBlacklisted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        repository.save(merchant);

        log.info("[merchant={}] 🟢 event=merchant_created", merchantName);

        return merchant;
    }

    // ----------------------------------------------------------------------
    // Mark merchant as blacklisted
    // ----------------------------------------------------------------------
    public Merchant blacklistMerchant(String merchantName) {

        Merchant merchant = repository.findByMerchantName(merchantName)
                .orElseThrow(() -> {
                    log.error("[merchant={}] ❌ event=merchant_blacklist_failed reason='not_found'", merchantName);
                    return new IllegalArgumentException("Merchant not found: " + merchantName);
                });

        if (merchant.isBlacklisted()) {
            log.warn("[merchant={}] ⚠️ event=merchant_already_blacklisted", merchantName);
            return merchant; // no-op
        }

        merchant.setBlacklisted(true);
        merchant.setUpdatedAt(Instant.now());
        repository.save(merchant);

        log.info("[merchant={}] 🔴 event=merchant_blacklisted", merchantName);

        return merchant;
    }

    // ----------------------------------------------------------------------
    // Remove a merchant from blacklist
    // ----------------------------------------------------------------------
    public Merchant unblacklistMerchant(String merchantName) {

        Merchant merchant = repository.findByMerchantName(merchantName)
                .orElseThrow(() -> {
                    log.error("[merchant={}] ❌ event=merchant_unblacklist_failed reason='not_found'", merchantName);
                    return new IllegalArgumentException("Merchant not found: " + merchantName);
                });

        if (!merchant.isBlacklisted()) {
            log.warn("[merchant={}] 🟡 event=merchant_not_blacklisted", merchantName);
            return merchant; // no-op
        }

        merchant.setBlacklisted(false);
        merchant.setUpdatedAt(Instant.now());
        repository.save(merchant);

        log.info("[merchant={}] 🟢 event=merchant_unblacklisted", merchantName);

        return merchant;
    }

    // ----------------------------------------------------------------------
    // Check if merchant is blacklisted
    // ----------------------------------------------------------------------
    public boolean isBlacklisted(String merchantName) {

        Merchant merchant = repository.findByMerchantName(merchantName)
                .orElseThrow(() -> {
                    log.error("[merchant={}] ❌ event=merchant_status_check_failed reason='not_found'", merchantName);
                    return new IllegalArgumentException("Merchant not found: " + merchantName);
                });

        boolean result = merchant.isBlacklisted();

        log.debug("[merchant={}] event=merchant_blacklist_check blacklisted={}", merchantName, result);

        return result;
    }
}
