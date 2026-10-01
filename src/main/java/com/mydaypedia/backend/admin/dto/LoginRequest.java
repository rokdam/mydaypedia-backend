package com.mydaypedia.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String password) {
}
