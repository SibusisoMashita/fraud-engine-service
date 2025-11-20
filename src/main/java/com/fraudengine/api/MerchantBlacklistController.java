package com.fraudengine.api;

import com.fraudengine.domain.MerchantBlacklist;
import com.fraudengine.service.MerchantBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blacklist")
@RequiredArgsConstructor
public class MerchantBlacklistController {

    private final MerchantBlacklistService service;

    @GetMapping
    public ResponseEntity<List<MerchantBlacklist>> list() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    public ResponseEntity<MerchantBlacklist> add(@RequestParam String merchantName) {
        return ResponseEntity.ok(service.add(merchantName));
    }

    @DeleteMapping
    public ResponseEntity<Void> remove(@RequestParam String merchantName) {
        service.remove(merchantName);
        return ResponseEntity.noContent().build();
    }
}
