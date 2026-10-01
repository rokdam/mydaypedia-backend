package com.mydaypedia.backend.artist;

import com.mydaypedia.backend.artist.dto.ArtistCreateRequest;
import com.mydaypedia.backend.artist.dto.ArtistResponse;
import com.mydaypedia.backend.common.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ArtistService {

    private final ArtistRepository artistRepository;

    @Transactional
    public ArtistResponse create(ArtistCreateRequest request) {
        Artist artist = artistRepository.save(new Artist(request.name(), request.bugsArtistId()));
        return ArtistResponse.from(artist);
    }

    public List<ArtistResponse> list() {
        return artistRepository.findAllByOrderByNameAsc().stream()
                .map(ArtistResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        if (!artistRepository.existsById(id)) {
            throw new ResourceNotFoundException("아티스트를 찾을 수 없습니다. id=" + id);
        }
        artistRepository.deleteById(id);
    }
}
