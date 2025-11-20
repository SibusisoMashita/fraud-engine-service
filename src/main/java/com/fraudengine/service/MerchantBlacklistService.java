package com.fraudengine.service;

import com.fraudengine.domain.MerchantBlacklist;
import com.fraudengine.repository.MerchantBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantBlacklistService {

    private final MerchantBlacklistRepository repository;

    public List<MerchantBlacklist> getAll() {
        return repository.findAll();
    }

    public MerchantBlacklist add(String merchantName) {
        if (repository.existsByMerchantName(merchantName)) {
            throw new IllegalStateException("Merchant already blacklisted: " + merchantName);
        }
        return repository.save(MerchantBlacklist.builder()
                .merchantName(merchantName)
                .build());
    }

    public void remove(String merchantName) {
        var entry = repository.findByMerchantName(merchantName)
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found: " + merchantName));

        repository.delete(entry);
    }

    public boolean isBlacklisted(String merchantName) {
        return repository.existsByMerchantName(merchantName);
    }
}
