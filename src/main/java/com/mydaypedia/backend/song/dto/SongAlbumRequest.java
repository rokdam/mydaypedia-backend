package com.mydaypedia.backend.song.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SongAlbumRequest(
        /** 기존 /api/albums에 등록된 id. */
        @NotNull Long albumId,
        @Positive Integer trackNumber,
        /** 벅스 트랙 id (선택, 앨범별로 다름). 다른 수록 정보와 겹치면 409. */
        Long bugsTrackId
) {
}
