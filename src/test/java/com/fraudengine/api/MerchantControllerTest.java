package com.fraudengine.api;

import com.fraudengine.domain.Merchant;
import com.fraudengine.service.MerchantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MerchantController.class)
class MerchantControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MerchantService service;

    @Test
    void shouldListMerchants() throws Exception {
        Merchant entry = Merchant.builder()
                .id(1L)
                .merchantName("StoreA")
                .isBlacklisted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(service.getAll()).thenReturn(List.of(entry));

        mvc.perform(get("/api/v1/merchants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].merchantName").value("StoreA"))
                .andExpect(jsonPath("$[0].blacklisted").value(false));
    }

    @Test
    void shouldCreateMerchant() throws Exception {
        Merchant entry = Merchant.builder()
                .id(1L)
                .merchantName("StoreA")
                .isBlacklisted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(service.createMerchant("StoreA")).thenReturn(entry);

        mvc.perform(post("/api/v1/merchants")
                .param("merchantName", "StoreA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantName").value("StoreA"))
                .andExpect(jsonPath("$.blacklisted").value(false));
    }

    @Test
    void shouldBlacklistMerchant() throws Exception {
        Merchant entry = Merchant.builder()
                .id(1L)
                .merchantName("StoreA")
                .isBlacklisted(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(service.blacklistMerchant("StoreA")).thenReturn(entry);

        mvc.perform(post("/api/v1/merchants/StoreA/blacklist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantName").value("StoreA"))
                .andExpect(jsonPath("$.blacklisted").value(true));
    }

    @Test
    void shouldUnblacklistMerchant() throws Exception {
        Merchant entry = Merchant.builder()
                .merchantName("StoreA")
                .isBlacklisted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(service.unblacklistMerchant("StoreA")).thenReturn(entry);

        mvc.perform(delete("/api/v1/merchants/StoreA/blacklist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantName").value("StoreA"))
                .andExpect(jsonPath("$.blacklisted").value(false));
    }

    @Test
    void shouldCheckBlacklistStatus() throws Exception {
        when(service.isBlacklisted("StoreA")).thenReturn(true);

        mvc.perform(get("/api/v1/merchants/StoreA/blacklist"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
