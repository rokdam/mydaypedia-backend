package com.mydaypedia.backend.song;

import com.mydaypedia.backend.album.Album;
import com.mydaypedia.backend.artist.Artist;
import com.mydaypedia.backend.songwriter.Songwriter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "songs")
@Getter
@Setter
@NoArgsConstructor
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    /** 제목 색인 글자(가나다 색인 필터용, TitleInitial). 저장할 때마다 제목에서 다시 계산한다. */
    @Column(name = "title_initial", length = 2)
    private String titleInitial;

    /** 수록 앨범들. 같은 곡이 여러 앨범에 실릴 수 있다 (싱글 → 정규 재수록 등). 비어 있으면 앨범 미지정. */
    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SongAlbum> albumLinks = new ArrayList<>();

    /** 이 곡을 부른 추적 아티스트(DAY6, 멤버 솔로 등). 추적 대상이 아닌 피처링 가수는 featuredArtist에 문자열로 둔다. */
    @ManyToMany
    @JoinTable(name = "song_artists",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id"))
    private Set<Artist> artists = new LinkedHashSet<>();

    /** 곡의 최초 발매일 (여러 앨범에 실렸으면 가장 먼저 나온 앨범 기준). */
    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(length = 50)
    private String genre;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "youtube_url", length = 500)
    private String youtubeUrl;

    @Column(length = 1000)
    private String memo;

    /** 피처링 아티스트명. null이면 피처링 없음. */
    @Column(name = "featured_artist", length = 200)
    private String featuredArtist;

    /**
     * 멜론 곡 번호 (melon.com/song/detail.htm?songId=...). 멜론 원클릭 재생 링크(담은 곡 → 플레이리스트)에 쓴다.
     * 곡 등록/수정(PUT) 요청에는 없고 전용 API(PUT /api/songs/{id}/melon-id)로만 바꾼다 — 기존 수집 스크립트의 PUT이 지우지 않게.
     */
    @Column(name = "melon_song_id")
    private Long melonSongId;

    @Enumerated(EnumType.STRING)
    @Column(name = "song_type", length = 20)
    private SongType songType = SongType.ORIGINAL;

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SongWriterCredit> writerCredits = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Song(String title, LocalDate releaseDate, String genre, Integer durationSeconds,
                String youtubeUrl, String memo, String featuredArtist, SongType songType) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.durationSeconds = durationSeconds;
        this.youtubeUrl = youtubeUrl;
        this.memo = memo;
        this.featuredArtist = featuredArtist;
        this.songType = songType == null ? SongType.ORIGINAL : songType;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.titleInitial = TitleInitial.of(title);
        if (this.songType == null) {
            this.songType = SongType.ORIGINAL;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.titleInitial = TitleInitial.of(title);
    }

    public void applyUpdate(String title, LocalDate releaseDate, String genre,
                             Integer durationSeconds, String youtubeUrl, String memo,
                             String featuredArtist, SongType songType) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.durationSeconds = durationSeconds;
        this.youtubeUrl = youtubeUrl;
        this.memo = memo;
        this.featuredArtist = featuredArtist;
        this.songType = songType == null ? SongType.ORIGINAL : songType;
    }

    public void replaceArtists(List<Artist> wanted) {
        artists.retainAll(wanted);
        artists.addAll(wanted);
    }

    public boolean isOnAlbum(Long albumId) {
        return albumLinks.stream().anyMatch(link -> link.getAlbum().getId().equals(albumId));
    }

    public void addAlbum(Album album, Integer trackNumber, Long bugsTrackId) {
        albumLinks.add(new SongAlbum(this, album, trackNumber, bugsTrackId));
    }

    /**
     * 수록 앨범을 주어진 목록으로 맞춘다. writerCredits와 같은 이유로(유니크 제약 + Hibernate가 INSERT를
     * DELETE보다 먼저 실행) 전부 지우고 다시 넣지 않고, 필요한 것만 지우고/고치고/추가한다.
     */
    public void replaceAlbums(List<AlbumPlacement> wanted) {
        List<Long> wantedIds = wanted.stream().map(p -> p.album().getId()).toList();
        albumLinks.removeIf(link -> !wantedIds.contains(link.getAlbum().getId()));
        for (AlbumPlacement placement : wanted) {
            albumLinks.stream()
                    .filter(link -> link.getAlbum().getId().equals(placement.album().getId()))
                    .findFirst()
                    .ifPresentOrElse(
                            link -> link.updateTrackInfo(placement.trackNumber(), placement.bugsTrackId()),
                            () -> addAlbum(placement.album(), placement.trackNumber(), placement.bugsTrackId()));
        }
    }

    /** 기존 작곡/작사 크레딧을 주어진 사람들로 맞춘다. */
    public void replaceWriterCredits(List<Songwriter> composers, List<Songwriter> lyricists) {
        replaceCreditsForRole(SongWriterRole.COMPOSER, composers);
        replaceCreditsForRole(SongWriterRole.LYRICIST, lyricists);
    }

    /**
     * 변경 없는 크레딧은 건드리지 않고, 더 이상 필요 없는 것만 지우고 새로 필요한 것만 추가한다.
     * (song_id, songwriter_id, role) 유니크 제약 때문에 무조건 clear() 후 전부 다시 add()하면,
     * Hibernate가 같은 flush에서 INSERT를 DELETE보다 먼저 실행해 안 바뀐 크레딧에서도
     * 중복 키 오류가 난다 — 실제로 겪은 문제.
     */
    private void replaceCreditsForRole(SongWriterRole role, List<Songwriter> wanted) {
        List<Long> wantedIds = wanted.stream().map(Songwriter::getId).toList();

        writerCredits.removeIf(credit -> credit.getRole() == role
                && !wantedIds.contains(credit.getSongwriter().getId()));

        List<Long> existingIds = writerCredits.stream()
                .filter(credit -> credit.getRole() == role)
                .map(credit -> credit.getSongwriter().getId())
                .toList();

        wanted.stream()
                .filter(songwriter -> !existingIds.contains(songwriter.getId()))
                .forEach(songwriter -> writerCredits.add(new SongWriterCredit(this, songwriter, role)));
    }

    public List<Songwriter> getComposers() {
        return writerCredits.stream()
                .filter(credit -> credit.getRole() == SongWriterRole.COMPOSER)
                .map(SongWriterCredit::getSongwriter)
                .toList();
    }

    public List<Songwriter> getLyricists() {
        return writerCredits.stream()
                .filter(credit -> credit.getRole() == SongWriterRole.LYRICIST)
                .map(SongWriterCredit::getSongwriter)
                .toList();
    }

    /** 수록 앨범 지정용 값 (앨범 + 트랙 번호 + 벅스 트랙 id). */
    public record AlbumPlacement(Album album, Integer trackNumber, Long bugsTrackId) {
    }
}
