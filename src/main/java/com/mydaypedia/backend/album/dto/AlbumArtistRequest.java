package com.mydaypedia.backend.album.dto;

import com.mydaypedia.backend.album.AlbumArtistRole;
import jakarta.validation.constraints.NotNull;

public record AlbumArtistRequest(
        /** 기존 /api/artists에 등록된 id. */
        @NotNull Long artistId,
        @NotNull AlbumArtistRole role
) {
}
