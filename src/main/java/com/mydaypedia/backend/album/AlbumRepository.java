package com.mydaypedia.backend.album;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    /**
     * 앨범 목록을 아티스트 연결까지 한 번에 가져온다 (N+1 방지).
     * artistId를 주면 그 아티스트와 연결된 앨범만, role까지 주면 그 관계로 연결된 앨범만 남긴다.
     * 필터를 fetch join 대상(l)에 직접 걸면 응답의 artists 목록까지 잘려 나가므로, EXISTS 서브쿼리로 거른다.
     */
    @Query("SELECT DISTINCT a FROM Album a "
            + "LEFT JOIN FETCH a.artistLinks l LEFT JOIN FETCH l.artist "
            + "WHERE (:artistId IS NULL OR EXISTS ("
            + "  SELECT 1 FROM AlbumArtist x WHERE x.album = a AND x.artist.id = :artistId "
            + "  AND (:role IS NULL OR x.role = :role))) "
            + "ORDER BY a.releaseDate, a.title")
    List<Album> findAllWithArtists(@Param("artistId") Long artistId, @Param("role") AlbumArtistRole role);

    @Query("SELECT a FROM Album a LEFT JOIN FETCH a.artistLinks l LEFT JOIN FETCH l.artist WHERE a.id = :id")
    Optional<Album> findDetailById(@Param("id") Long id);
}
