package com.mydaypedia.backend.album;

import com.mydaypedia.backend.album.dto.AlbumArtistRequest;
import com.mydaypedia.backend.album.dto.AlbumCreateRequest;
import com.mydaypedia.backend.album.dto.AlbumResponse;
import com.mydaypedia.backend.artist.Artist;
import com.mydaypedia.backend.artist.ArtistRepository;
import com.mydaypedia.backend.common.exception.DuplicateResourceException;
import com.mydaypedia.backend.common.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 응답 DTO 매핑은 반드시 이 서비스(트랜잭션) 안에서 끝낸다.
 * open-in-view=false이므로, 지연 로딩된 Album.artistLinks를 컨트롤러에서 접근하면 LazyInitializationException이 발생한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;

    @Transactional
    public AlbumResponse create(AlbumCreateRequest request) {
        Album album = albumRepository.save(new Album(
                request.title(),
                request.releaseDate(),
                request.albumType(),
                request.artistName(),
                request.bugsAlbumId()
        ));
        return AlbumResponse.from(album);
    }

    public List<AlbumResponse> list(Long artistId, AlbumArtistRole role) {
        return albumRepository.findAllWithArtists(artistId, role).stream()
                .map(AlbumResponse::from)
                .toList();
    }

    @Transactional
    public AlbumResponse addArtist(Long albumId, AlbumArtistRequest request) {
        Album album = findDetail(albumId);
        Artist artist = artistRepository.findById(request.artistId())
                .orElseThrow(() -> new ResourceNotFoundException("아티스트를 찾을 수 없습니다. id=" + request.artistId()));
        if (album.hasArtist(artist.getId())) {
            throw new DuplicateResourceException(
                    "이미 연결된 아티스트입니다. albumId=" + albumId + ", artistId=" + artist.getId());
        }
        album.addArtist(artist, request.role());
        return AlbumResponse.from(album);
    }

    @Transactional
    public void removeArtist(Long albumId, Long artistId) {
        Album album = findDetail(albumId);
        if (!album.removeArtist(artistId)) {
            throw new ResourceNotFoundException(
                    "앨범에 연결되지 않은 아티스트입니다. albumId=" + albumId + ", artistId=" + artistId);
        }
    }

    @Transactional
    public void delete(Long id) {
        if (!albumRepository.existsById(id)) {
            throw new ResourceNotFoundException("앨범을 찾을 수 없습니다. id=" + id);
        }
        albumRepository.deleteById(id);
    }

    private Album findDetail(Long id) {
        return albumRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("앨범을 찾을 수 없습니다. id=" + id));
    }
}
