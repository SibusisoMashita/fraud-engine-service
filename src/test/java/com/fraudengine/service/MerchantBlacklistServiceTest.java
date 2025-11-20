package com.fraudengine.service;

import com.fraudengine.domain.MerchantBlacklist;
import com.fraudengine.repository.MerchantBlacklistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantBlacklistServiceTest {

    @Mock
    private MerchantBlacklistRepository repository;

    @InjectMocks
    private MerchantBlacklistService service;

    @Test
    void shouldReturnAllMerchants() {
        when(repository.findAll()).thenReturn(List.of(new MerchantBlacklist()));

        var result = service.getAll();

        assertEquals(1, result.size());
        verify(repository).findAll();
    }

    @Test
    void shouldAddMerchant_WhenNotExists() {
        when(repository.existsByMerchantName("StoreA")).thenReturn(false);

        service.add("StoreA");

        verify(repository).save(any(MerchantBlacklist.class));
    }

    @Test
    void shouldThrow_WhenAddingDuplicate() {
        when(repository.existsByMerchantName("StoreA")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.add("StoreA"));
    }

    @Test
    void shouldRemoveMerchant_WhenFound() {
        MerchantBlacklist entry = MerchantBlacklist.builder()
                .merchantName("StoreA")
                .build();

        when(repository.findByMerchantName("StoreA"))
                .thenReturn(Optional.of(entry));

        service.remove("StoreA");

        verify(repository).delete(entry);
    }

    @Test
    void shouldThrow_WhenMerchantNotFound() {
        when(repository.findByMerchantName("StoreA"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.remove("StoreA"));
    }

    @Test
    void shouldCheckBlacklist() {
        when(repository.existsByMerchantName("StoreA")).thenReturn(true);

        assertTrue(service.isBlacklisted("StoreA"));
    }
}
