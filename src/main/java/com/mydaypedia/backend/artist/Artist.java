package com.mydaypedia.backend.artist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 이 서비스가 추적하는 아티스트 — 그룹(DAY6)과 멤버 솔로(Young K 등), 유닛(Even of Day 등).
 * 앨범과의 관계(발매/참여/컴필레이션)는 album.AlbumArtist가 역할과 함께 관리한다.
 * Album.artistName(벅스에 표시되는 대표 아티스트 문자열)과 달리, 여기에는 직접 추적하는 아티스트만 등록한다.
 */
@Entity
@Table(name = "artists")
@Getter
@Setter
@NoArgsConstructor
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** 벅스 아티스트 id. 출처 추적 + 수집 스크립트가 같은 아티스트를 다시 만들지 않도록 하는 키. */
    @Column(name = "bugs_artist_id", unique = true)
    private Long bugsArtistId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Artist(String name, Long bugsArtistId) {
        this.name = name;
        this.bugsArtistId = bugsArtistId;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
