package com.fraudengine.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = GlobalExceptionHandlerTestController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@Import(GlobalExceptionHandler.class) // <-- include your handler
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    // 1️⃣ MethodArgumentNotValidException (400)
    @Test
    void shouldHandleValidationErrors() throws Exception {
        var body = mapper.createObjectNode();
        body.put("value", 0); // violates @Min(1)

        mvc.perform(post("/test/error/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.fields.value")
                        .value("value must be >= 1"));
    }

    // 2️⃣ ConstraintViolationException (400)
    @Test
    void shouldHandleConstraintViolation() throws Exception {
        mvc.perform(get("/test/error/constraint")
                .param("value", "1")) // violates @Min(5)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Constraint violation"));
    }

    // 3️⃣ IllegalArgumentException → 404
    @Test
    void shouldHandleIllegalArgument() throws Exception {
        mvc.perform(get("/test/error/illegal-arg"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Not found error"));
    }

    // 4️⃣ IllegalStateException → 409
    @Test
    void shouldHandleIllegalState() throws Exception {
        mvc.perform(get("/test/error/illegal-state"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Conflict happened"));
    }
}
