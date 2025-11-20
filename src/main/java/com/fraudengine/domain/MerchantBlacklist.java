package com.fraudengine.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "merchant_blacklist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_name", unique = true, nullable = false, length = 128)
    private String merchantName;
}
