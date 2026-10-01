package com.mydaypedia.backend.playlist.dto;

import jakarta.validation.constraints.NotBlank;

public record PlaylistCreateRequest(
        @NotBlank String name,
        String description
) {
}
