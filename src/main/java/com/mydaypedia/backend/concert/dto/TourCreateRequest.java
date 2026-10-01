package com.mydaypedia.backend.concert.dto;

import com.mydaypedia.backend.concert.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TourCreateRequest(
        @NotBlank String name,
        @NotNull EventType eventType,
        String memo
) {
}
