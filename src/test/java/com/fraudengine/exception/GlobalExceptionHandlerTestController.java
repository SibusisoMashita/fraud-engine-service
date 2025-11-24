package com.fraudengine.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test/error")
@Validated // Needed for ConstraintViolationException
class GlobalExceptionHandlerTestController {

    @PostMapping("/validation")
    public void triggerValidation(@Valid @RequestBody TestDto dto) { }

    @GetMapping("/constraint")
    public void triggerConstraint(@RequestParam @Min(5) int value) { }

    @GetMapping("/illegal-arg")
    public void triggerIllegalArgument() {
        throw new IllegalArgumentException("Not found error");
    }

    @GetMapping("/illegal-state")
    public void triggerIllegalState() {
        throw new IllegalStateException("Conflict happened");
    }

    // DTO with validation rules
    static class TestDto {
        @Min(value = 1, message = "value must be >= 1")
        public int value;
    }
}
