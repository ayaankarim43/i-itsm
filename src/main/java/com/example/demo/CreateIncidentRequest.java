package com.example.demo;

import jakarta.validation.constraints.*;

public record CreateIncidentRequest(
        @NotBlank String description,
        @NotNull Long serviceId,
        Long categoryId,
        @Min(1) @Max(5) int impact,
        @Min(1) @Max(5) int urgency) {}
