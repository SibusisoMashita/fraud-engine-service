package com.fraudengine;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

class FraudEngineServiceApplicationTest {

    @Test
    void mainMethodShouldRun() {
        FraudEngineServiceApplication.main(new String[] {});
    }

    @Nested
    @SpringBootTest
    class ContextLoadTest {
        @Test
        void contextLoads() { }
    }
}
