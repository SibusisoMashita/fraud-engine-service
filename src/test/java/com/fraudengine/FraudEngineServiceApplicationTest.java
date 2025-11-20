package com.fraudengine;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.*;

class FraudEngineServiceApplicationTest {

    @Test
    void mainMethodShouldRun() {
        try (MockedStatic<SpringApplication> mock = mockStatic(SpringApplication.class)) {
            mock.when(() -> SpringApplication.run(FraudEngineServiceApplication.class, new String[]{}))
                .thenReturn(null);

            FraudEngineServiceApplication.main(new String[]{});

            mock.verify(() -> SpringApplication.run(FraudEngineServiceApplication.class, new String[]{}));
        }
    }
}
