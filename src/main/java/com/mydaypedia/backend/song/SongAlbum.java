package com.mydaypedia.backend.song;

import com.mydaypedia.backend.album.Album;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 곡의 수록 앨범 (Song—Album N:M). 같은 곡이 여러 앨범에 실릴 수 있다
 * (예: Every DAY6 월간 싱글에 먼저 나온 곡이 정규 SUNRISE에 다시 수록).
 * 벅스는 앨범마다 트랙 id가 다르므로 bugsTrackId는 곡이 아니라 이 수록 정보에 붙는다.
 */
@Entity
@Table(name = "song_albums",
        uniqueConstraints = @UniqueConstraint(columnNames = {"song_id", "album_id"}))
@Getter
@NoArgsConstructor
public class SongAlbum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    /** 앨범 내 트랙 번호. */
    @Column(name = "track_number")
    private Integer trackNumber;

    /** 벅스 트랙 id (앨범별로 다름). 출처 추적 + 재수집 시 중복 방지용. */
    @Column(name = "bugs_track_id", unique = true)
    private Long bugsTrackId;

    SongAlbum(Song song, Album album, Integer trackNumber, Long bugsTrackId) {
        this.song = song;
        this.album = album;
        this.trackNumber = trackNumber;
        this.bugsTrackId = bugsTrackId;
    }

    void updateTrackInfo(Integer trackNumber, Long bugsTrackId) {
        this.trackNumber = trackNumber;
        this.bugsTrackId = bugsTrackId;
    }
}
