package com.mydaypedia.backend.song;

import com.mydaypedia.backend.album.Album;
import com.mydaypedia.backend.album.AlbumRepository;
import com.mydaypedia.backend.artist.Artist;
import com.mydaypedia.backend.artist.ArtistRepository;
import com.mydaypedia.backend.common.exception.DuplicateResourceException;
import com.mydaypedia.backend.common.exception.ResourceNotFoundException;
import com.mydaypedia.backend.song.dto.SongAlbumRequest;
import com.mydaypedia.backend.song.dto.SongCreateRequest;
import com.mydaypedia.backend.song.dto.SongResponse;
import com.mydaypedia.backend.song.dto.SongUpdateRequest;
import com.mydaypedia.backend.songwriter.Songwriter;
import com.mydaypedia.backend.songwriter.SongwriterRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 응답 DTO 매핑은 반드시 이 서비스(트랜잭션) 안에서 끝낸다.
 * open-in-view=false이므로, 지연 로딩된 연관관계(Song.albumLinks, Song.artists, Song.writerCredits)를
 * 트랜잭션이 끝난 뒤 컨트롤러에서 접근하면 LazyInitializationException이 발생한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SongService {

    private final SongRepository songRepository;
    private final SongwriterRepository songwriterRepository;
    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;

    @Transactional
    public SongResponse create(SongCreateRequest request) {
        Song song = new Song(
                request.title(),
                request.releaseDate(),
                request.genre(),
                request.durationSeconds(),
                request.youtubeUrl(),
                request.memo(),
                request.featuredArtist(),
                request.songType()
        );
        song.replaceAlbums(resolveAlbumPlacements(request.albums()));
        song.replaceArtists(resolveArtists(request.artistIds()));
        song.replaceWriterCredits(
                resolveSongwriters(request.composerIds()),
                resolveSongwriters(request.lyricistIds())
        );
        return SongResponse.from(songRepository.save(song));
    }

    public SongResponse get(Long id) {
        return SongResponse.from(findDetail(id));
    }

    public List<SongResponse> search(SongSearchCriteria criteria) {
        Specification<Song> spec = Specification.allOf(
                SongSpecifications.titleContains(criteria.title()),
                SongSpecifications.initialEquals(criteria.initial()),
                SongSpecifications.trackNumberEquals(criteria.trackNumber()),
                SongSpecifications.albumTitleContains(criteria.album()),
                SongSpecifications.albumIdEquals(criteria.albumId()),
                SongSpecifications.albumTypeEquals(criteria.albumType()),
                SongSpecifications.sungBy(criteria.artistId()),
                SongSpecifications.genreEquals(criteria.genre()),
                SongSpecifications.songTypeEquals(criteria.songType()),
                SongSpecifications.hasFeaturing(criteria.hasFeaturing()),
                SongSpecifications.writtenBy(criteria.composerIds(), criteria.composerMatch(), SongWriterRole.COMPOSER),
                SongSpecifications.writtenBy(criteria.lyricistIds(), criteria.lyricistMatch(), SongWriterRole.LYRICIST),
                SongSpecifications.releaseYearEquals(criteria.year()),
                SongSpecifications.releaseMonthEquals(criteria.month()),
                SongSpecifications.releaseDayEquals(criteria.day())
        );
        return songRepository.findAll(spec).stream()
                .map(SongResponse::from)
                .toList();
    }

    @Transactional
    public SongResponse update(Long id, SongUpdateRequest request) {
        Song song = findDetail(id);
        song.applyUpdate(
                request.title(),
                request.releaseDate(),
                request.genre(),
                request.durationSeconds(),
                request.youtubeUrl(),
                request.memo(),
                request.featuredArtist(),
                request.songType()
        );
        song.replaceAlbums(resolveAlbumPlacements(request.albums()));
        song.replaceArtists(resolveArtists(request.artistIds()));
        song.replaceWriterCredits(
                resolveSongwriters(request.composerIds()),
                resolveSongwriters(request.lyricistIds())
        );
        return SongResponse.from(song);
    }

    /** 기존 곡에 수록 앨범 하나를 추가한다 (예: 싱글로 먼저 나온 곡이 정규에 재수록). */
    @Transactional
    public SongResponse addAlbum(Long id, SongAlbumRequest request) {
        Song song = findDetail(id);
        if (song.isOnAlbum(request.albumId())) {
            throw new DuplicateResourceException(
                    "이미 이 앨범에 수록된 곡입니다. songId=" + id + ", albumId=" + request.albumId());
        }
        song.addAlbum(resolveAlbum(request.albumId()), request.trackNumber(), request.bugsTrackId());
        return SongResponse.from(song);
    }

    @Transactional
    public SongResponse updateMelonSongId(Long id, Long melonSongId) {
        Song song = findDetail(id);
        song.setMelonSongId(melonSongId);
        return SongResponse.from(song);
    }

    @Transactional
    public void delete(Long id) {
        if (!songRepository.existsById(id)) {
            throw new ResourceNotFoundException("곡을 찾을 수 없습니다. id=" + id);
        }
        songRepository.deleteById(id);
    }

    private Song findDetail(Long id) {
        return songRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("곡을 찾을 수 없습니다. id=" + id));
    }

    private Album resolveAlbum(Long albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("앨범을 찾을 수 없습니다. id=" + albumId));
    }

    private List<Song.AlbumPlacement> resolveAlbumPlacements(List<SongAlbumRequest> albums) {
        if (albums == null || albums.isEmpty()) {
            return List.of();
        }
        return albums.stream()
                .map(a -> new Song.AlbumPlacement(resolveAlbum(a.albumId()), a.trackNumber(), a.bugsTrackId()))
                .toList();
    }

    private List<Artist> resolveArtists(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Artist> found = artistRepository.findAllById(ids);
        if (found.size() != ids.size()) {
            throw new ResourceNotFoundException("존재하지 않는 아티스트 id가 포함되어 있습니다.");
        }
        return found;
    }

    private List<Songwriter> resolveSongwriters(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Songwriter> found = songwriterRepository.findAllById(ids);
        if (found.size() != ids.size()) {
            throw new ResourceNotFoundException("존재하지 않는 작곡가/작사가 id가 포함되어 있습니다.");
        }
        return found;
    }
}
