package com.fraudengine.config;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void openApiBeanShouldBeCreated() {
        OpenApiConfig config = new OpenApiConfig();
        var api = config.customOpenAPI(); // adjust method name as needed
        assertThat(api).isNotNull();
        assertThat(api.getInfo().getTitle()).isNotEmpty();
    }
}
