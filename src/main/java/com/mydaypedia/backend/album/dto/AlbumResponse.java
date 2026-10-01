package com.mydaypedia.backend.album.dto;

import com.mydaypedia.backend.album.Album;
import com.mydaypedia.backend.album.AlbumType;
import java.time.LocalDate;
import java.util.List;

public record AlbumResponse(
        Long id,
        String title,
        LocalDate releaseDate,
        AlbumType albumType,
        String artistName,
        Long bugsAlbumId,
        List<AlbumArtistResponse> artists
) {

    /** album의 artistLinks(와 각 artist)는 반드시 트랜잭션(서비스 계층) 안에서 초기화된 상태로 넘어와야 한다. */
    public static AlbumResponse from(Album album) {
        return new AlbumResponse(
                album.getId(),
                album.getTitle(),
                album.getReleaseDate(),
                album.getAlbumType(),
                album.getArtistName(),
                album.getBugsAlbumId(),
                album.getArtistLinks().stream().map(AlbumArtistResponse::from).toList()
        );
    }
}
