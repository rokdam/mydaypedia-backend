package com.mydaypedia.backend.album.dto;

import jakarta.validation.constraints.NotBlank;
import com.mydaypedia.backend.album.AlbumType;
import java.time.LocalDate;

public record AlbumCreateRequest(
        @NotBlank String title,
        LocalDate releaseDate,
        AlbumType albumType,
        String artistName,
        /** 벅스 앨범 id (선택). 같은 값으로 두 번 등록하면 409. */
        Long bugsAlbumId
) {
}
