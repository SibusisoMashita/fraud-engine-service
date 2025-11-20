package com.fraudengine.repository;

import com.fraudengine.domain.MerchantBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantBlacklistRepository extends JpaRepository<MerchantBlacklist, Long> {

    boolean existsByMerchantName(String merchantName);

    Optional<MerchantBlacklist> findByMerchantName(String merchantName);
}
