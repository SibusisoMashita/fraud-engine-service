package com.fraudengine.repository;

import com.fraudengine.domain.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    boolean existsByMerchantName(String merchantName);

    Optional<Merchant> findByMerchantName(String merchantName);
}
