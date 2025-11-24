package com.fraudengine.api;

import com.fraudengine.domain.MerchantBlacklist;
import com.fraudengine.service.MerchantBlacklistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MerchantBlacklistController.class)
class MerchantBlacklistControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MerchantBlacklistService service;

    @Test
    void shouldListMerchants() throws Exception {
        MerchantBlacklist entry = MerchantBlacklist.builder()
                .merchantName("StoreA")
                .build();

        when(service.getAll()).thenReturn(List.of(entry));

        mvc.perform(get("/api/v1/blacklist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].merchantName").value("StoreA"));
    }

    @Test
    void shouldAddMerchant() throws Exception {
        MerchantBlacklist entry = MerchantBlacklist.builder()
                .merchantName("StoreA")
                .build();

        when(service.add("StoreA")).thenReturn(entry);

        mvc.perform(post("/api/v1/blacklist")
                .param("merchantName", "StoreA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantName").value("StoreA"));
    }

    @Test
    void shouldRemoveMerchant() throws Exception {
        mvc.perform(delete("/api/v1/blacklist")
                .param("merchantName", "StoreA"))
                .andExpect(status().isNoContent());

        verify(service).remove("StoreA");
    }
}
