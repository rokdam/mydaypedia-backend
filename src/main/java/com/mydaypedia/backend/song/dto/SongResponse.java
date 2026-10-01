package com.mydaypedia.backend.song.dto;

import com.mydaypedia.backend.artist.dto.ArtistResponse;
import com.mydaypedia.backend.song.Song;
import com.mydaypedia.backend.song.SongAlbum;
import com.mydaypedia.backend.song.SongType;
import com.mydaypedia.backend.songwriter.dto.SongwriterResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record SongResponse(
        Long id,
        String title,
        /** 제목 색인 글자 (ㄱ~ㅎ, A~Z, #) */
        String titleInitial,
        List<SongAlbumResponse> albums,
        List<ArtistResponse> artists,
        LocalDate releaseDate,
        String genre,
        Integer durationSeconds,
        String youtubeUrl,
        String memo,
        List<SongwriterResponse> composers,
        List<SongwriterResponse> lyricists,
        String featuredArtist,
        SongType songType,
        /** 멜론 곡 번호. 아직 모르면 null (멜론 링크에서 빠진다). */
        Long melonSongId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /** song의 albumLinks, artists, writerCredits(작곡/작사)는 반드시 트랜잭션(서비스 계층) 안에서 초기화된 상태로 넘어와야 한다. */
    public static SongResponse from(Song song) {
        return new SongResponse(
                song.getId(),
                song.getTitle(),
                song.getTitleInitial(),
                song.getAlbumLinks().stream()
                        .sorted(Comparator.comparing((SongAlbum link) -> link.getAlbum().getReleaseDate(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(SongAlbumResponse::from)
                        .toList(),
                song.getArtists().stream().map(ArtistResponse::from).toList(),
                song.getReleaseDate(),
                song.getGenre(),
                song.getDurationSeconds(),
                song.getYoutubeUrl(),
                song.getMemo(),
                song.getComposers().stream().map(SongwriterResponse::from).toList(),
                song.getLyricists().stream().map(SongwriterResponse::from).toList(),
                song.getFeaturedArtist(),
                song.getSongType(),
                song.getMelonSongId(),
                song.getCreatedAt(),
                song.getUpdatedAt()
        );
    }
}
