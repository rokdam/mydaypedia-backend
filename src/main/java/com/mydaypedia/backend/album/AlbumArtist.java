package com.mydaypedia.backend.album;

import com.mydaypedia.backend.artist.Artist;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Album과 Artist를 관계(발매/참여/컴필레이션)와 함께 잇는 N:M 조인 엔티티.
 * 한 앨범이 여러 아티스트와 서로 다른 관계로 연결될 수 있다 (예: Every DAY6는 DAY6의 RELEASE이자 Young K의 JOIN).
 * 한 앨범-아티스트 쌍에는 관계가 하나만 붙는다.
 */
@Entity
@Table(name = "album_artists",
        uniqueConstraints = @UniqueConstraint(columnNames = {"album_id", "artist_id"}))
@Getter
@NoArgsConstructor
public class AlbumArtist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlbumArtistRole role;

    AlbumArtist(Album album, Artist artist, AlbumArtistRole role) {
        this.album = album;
        this.artist = artist;
        this.role = role;
    }
}
