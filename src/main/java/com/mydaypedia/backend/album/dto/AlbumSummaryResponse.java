package com.mydaypedia.backend.album.dto;

import com.mydaypedia.backend.album.Album;
import com.mydaypedia.backend.album.AlbumType;
import java.time.LocalDate;

/**
 * 곡 응답에 끼워 넣는 앨범 요약. 아티스트 연결(artists)은 빼서, 곡 검색 결과마다
 * album.artistLinks를 추가로 지연 로딩하는 N+1이 생기지 않게 한다.
 */
public record AlbumSummaryResponse(
        Long id,
        String title,
        LocalDate releaseDate,
        AlbumType albumType,
        String artistName,
        Long bugsAlbumId
) {

    public static AlbumSummaryResponse from(Album album) {
        return new AlbumSummaryResponse(
                album.getId(),
                album.getTitle(),
                album.getReleaseDate(),
                album.getAlbumType(),
                album.getArtistName(),
                album.getBugsAlbumId()
        );
    }
}
