package com.mydaypedia.backend.artist.dto;

import jakarta.validation.constraints.NotBlank;

public record ArtistCreateRequest(
        @NotBlank String name,
        /** 벅스 아티스트 id (선택). 같은 값으로 두 번 등록하면 409. */
        Long bugsArtistId
) {
}
