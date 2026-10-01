package com.mydaypedia.backend.album;

import com.mydaypedia.backend.artist.Artist;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 곡이 수록된 앨범. Song이 album_id로 참조한다 (한 앨범에 여러 곡, 곡은 앨범 없이도 존재 가능).
 * 발매일은 앨범 기준 값이고, 선공개 싱글처럼 곡마다 다를 수 있는 발매일은 Song.releaseDate에 따로 둔다.
 * DAY6가 참여만 한 앨범(다른 가수의 싱글, 트리뷰트/컴필레이션 등)도 함께 관리하므로 대표 아티스트를 따로 둔다.
 * 어떤 추적 아티스트(DAY6/멤버 솔로)와 어떤 관계인지는 artistLinks(AlbumArtist)로 관리한다.
 */
@Entity
@Table(name = "albums")
@Getter
@Setter
@NoArgsConstructor
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "album_type", length = 20)
    private AlbumType albumType;

    /** 앨범의 대표 아티스트명. DAY6 본인 발매작이 아니면 다른 이름이 들어간다 (예: 참여곡이 실린 다른 가수의 싱글). */
    @Column(name = "artist_name", length = 200)
    private String artistName;

    /** 벅스 앨범 id. 출처 추적 + 같은 앨범을 다시 가져올 때 중복 저장 방지용. */
    @Column(name = "bugs_album_id", unique = true)
    private Long bugsAlbumId;

    /** 이 앨범과 연결된 추적 아티스트들(DAY6, 멤버 솔로 등)과 그 관계. */
    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlbumArtist> artistLinks = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Album(String title, LocalDate releaseDate, AlbumType albumType, String artistName, Long bugsAlbumId) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.albumType = albumType;
        this.artistName = artistName;
        this.bugsAlbumId = bugsAlbumId;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean hasArtist(Long artistId) {
        return artistLinks.stream().anyMatch(link -> link.getArtist().getId().equals(artistId));
    }

    public void addArtist(Artist artist, AlbumArtistRole role) {
        artistLinks.add(new AlbumArtist(this, artist, role));
    }

    /** @return 연결이 있어서 지웠으면 true */
    public boolean removeArtist(Long artistId) {
        return artistLinks.removeIf(link -> link.getArtist().getId().equals(artistId));
    }
}
