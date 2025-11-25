package com.fraudengine.service;

import com.fraudengine.domain.Merchant;
import com.fraudengine.repository.MerchantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    private MerchantRepository repository;

    @InjectMocks
    private MerchantService service;

    @Test
    void shouldReturnAllMerchants() {
        when(repository.findAll()).thenReturn(List.of(new Merchant()));

        var result = service.getAll();

        assertEquals(1, result.size());
        verify(repository).findAll();
    }

    @Test
    void shouldCreateMerchant_WhenNotExists() {
        when(repository.existsByMerchantName("StoreA")).thenReturn(false);

        service.createMerchant("StoreA");

        verify(repository).save(any(Merchant.class));
    }

    @Test
    void shouldThrow_WhenCreatingDuplicate() {
        when(repository.existsByMerchantName("StoreA")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.createMerchant("StoreA"));
    }

    @Test
    void shouldBlacklistMerchant_WhenExists() {
        Merchant merchant = Merchant.builder()
                .merchantName("StoreA")
                .isBlacklisted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(repository.findByMerchantName("StoreA"))
                .thenReturn(Optional.of(merchant));

        service.blacklistMerchant("StoreA");

        assertTrue(merchant.isBlacklisted());
        verify(repository).save(merchant);
    }

    @Test
    void shouldThrow_WhenBlacklistingNonExistingMerchant() {
        when(repository.findByMerchantName("StoreA"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.blacklistMerchant("StoreA"));
    }

    @Test
    void shouldUnblacklistMerchant_WhenBlacklisted() {
        Merchant merchant = Merchant.builder()
                .merchantName("StoreA")
                .isBlacklisted(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(repository.findByMerchantName("StoreA"))
                .thenReturn(Optional.of(merchant));

        service.unblacklistMerchant("StoreA");

        assertFalse(merchant.isBlacklisted());
        verify(repository).save(merchant);
    }

    @Test
    void shouldThrow_WhenUnblacklistingNonExistingMerchant() {
        when(repository.findByMerchantName("StoreA"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.unblacklistMerchant("StoreA"));
    }

    @Test
    void shouldCheckBlacklistStatus() {
        Merchant merchant = Merchant.builder()
                .merchantName("StoreA")
                .isBlacklisted(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(repository.findByMerchantName("StoreA"))
                .thenReturn(Optional.of(merchant));

        assertTrue(service.isBlacklisted("StoreA"));
    }

    @Test
    void shouldThrow_WhenCheckingStatusOfUnknownMerchant() {
        when(repository.findByMerchantName("StoreA"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.isBlacklisted("StoreA"));
    }
}
