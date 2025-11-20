package com.fraudengine.service;

import com.fraudengine.domain.RuleConfig;
import com.fraudengine.repository.RuleConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleConfigServiceTest {

    @Mock
    private RuleConfigRepository repository;

    @InjectMocks
    private RuleConfigService service;

    @Test
    void shouldReturnConfigMap() {
        RuleConfig c1 = new RuleConfig("HIGH_VALUE", "threshold", "1000");

        when(repository.findByRuleName("HIGH_VALUE"))
                .thenReturn(List.of(c1));

        Map<String, String> config = service.getConfig("HIGH_VALUE");

        assertEquals("1000", config.get("threshold"));
    }

    @Test
    void shouldUpdateConfig() {
        service.updateConfig("R1", "key", "value");

        ArgumentCaptor<RuleConfig> captor = ArgumentCaptor.forClass(RuleConfig.class);

        verify(repository).save(captor.capture());

        RuleConfig saved = captor.getValue();
        assertEquals("R1", saved.getRuleName());
        assertEquals("key", saved.getConfigKey());
        assertEquals("value", saved.getConfigValue());
    }
}
