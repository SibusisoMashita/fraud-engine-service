package com.fraudengine.api;

import com.fraudengine.domain.Merchant;
import com.fraudengine.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService service;

    // Get all merchants (blacklisted + normal)
    @GetMapping
    public ResponseEntity<List<Merchant>> list() {
        return ResponseEntity.ok(service.getAll());
    }

    // Create a new merchant
    @PostMapping
    public ResponseEntity<Merchant> create(@RequestParam String merchantName) {
        return ResponseEntity.ok(service.createMerchant(merchantName));
    }

    // Mark a merchant as blacklisted
    @PostMapping("/{merchantName}/blacklist")
    public ResponseEntity<Merchant> blacklist(@PathVariable String merchantName) {
        return ResponseEntity.ok(service.blacklistMerchant(merchantName));
    }

    // Remove a merchant from blacklist (set flag false)
    @DeleteMapping("/{merchantName}/blacklist")
    public ResponseEntity<Merchant> unblacklist(@PathVariable String merchantName) {
        return ResponseEntity.ok(service.unblacklistMerchant(merchantName));
    }

    // Check blacklist status
    @GetMapping("/{merchantName}/blacklist")
    public ResponseEntity<Boolean> isBlacklisted(@PathVariable String merchantName) {
        return ResponseEntity.ok(service.isBlacklisted(merchantName));
    }
}
