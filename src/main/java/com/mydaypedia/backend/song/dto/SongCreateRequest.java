package com.mydaypedia.backend.song.dto;

import com.mydaypedia.backend.song.SongType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

public record SongCreateRequest(
        @NotBlank String title,
        /** 수록 앨범 목록 (여러 앨범 가능). 비우면 앨범 미지정. */
        List<@Valid SongAlbumRequest> albums,
        /** 부른 아티스트 — 기존 /api/artists에 등록된 id 목록. */
        List<Long> artistIds,
        LocalDate releaseDate,
        String genre,
        @Positive Integer durationSeconds,
        String youtubeUrl,
        String memo,
        String featuredArtist,
        SongType songType,
        /** 기존 /api/songwriters에 등록된 id 목록. */
        List<Long> composerIds,
        List<Long> lyricistIds
) {
}
