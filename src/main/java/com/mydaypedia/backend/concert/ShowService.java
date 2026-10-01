package com.mydaypedia.backend.concert;

import com.mydaypedia.backend.artist.Artist;
import com.mydaypedia.backend.artist.ArtistRepository;
import com.mydaypedia.backend.common.exception.ResourceNotFoundException;
import com.mydaypedia.backend.concert.dto.SetlistEntryRequest;
import com.mydaypedia.backend.concert.dto.ShowCreateRequest;
import com.mydaypedia.backend.concert.dto.ShowDetailResponse;
import com.mydaypedia.backend.concert.dto.ShowSummaryResponse;
import com.mydaypedia.backend.song.Song;
import com.mydaypedia.backend.song.SongRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** open-in-view=false라서 Show의 연관관계(tour, artists, setlist, setlist.song)는 이 서비스 안에서 DTO로 바꾼다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ShowService {

    private final ShowRepository showRepository;
    private final TourService tourService;
    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;

    @Transactional
    public ShowDetailResponse create(ShowCreateRequest request) {
        Tour tour = request.tourId() == null ? null : tourService.find(request.tourId());
        EventType eventType = request.eventType() != null ? request.eventType() : tour == null ? null : tour.getEventType();
        if (eventType == null) {
            throw new IllegalArgumentException("투어가 없는 공연은 eventType이 필요합니다.");
        }

        Show show = new Show();
        show.setTour(tour);
        show.setEventType(eventType);
        show.setTitle(request.title());
        show.setShowDate(request.showDate());
        show.setStartTime(request.startTime());
        show.setVenue(request.venue());
        show.setCity(request.city());
        show.setCountry(request.country());
        show.setMemo(request.memo());
        show.setSourceUrl(request.sourceUrl());
        show.replaceArtists(resolveArtists(request.artistIds()));
        fillSetlist(show, request.setlist());
        return ShowDetailResponse.from(showRepository.save(show));
    }

    public List<ShowSummaryResponse> search(ShowSearchCriteria criteria) {
        Specification<Show> spec = Specification.allOf(
                ShowSpecifications.tourIdEquals(criteria.tourId()),
                ShowSpecifications.eventTypeEquals(criteria.eventType()),
                ShowSpecifications.performedBy(criteria.artistId()),
                ShowSpecifications.yearEquals(criteria.year()),
                ShowSpecifications.setlistContains(criteria.songId())
        );
        return showRepository.findAll(spec, Sort.by("showDate", "startTime")).stream()
                .map(ShowSummaryResponse::from)
                .toList();
    }

    public ShowDetailResponse get(Long id) {
        return ShowDetailResponse.from(find(id));
    }

    /**
     * 세트리스트를 통째로 교체한다.
     * (show_id, position) 유니크 제약이 있어서, 비운 뒤 flush하지 않으면 Hibernate가 새 INSERT를 옛 DELETE보다 먼저 실행해
     * 같은 position에서 중복 키 오류가 난다 (Song 크레딧에서 실제로 겪은 것과 같은 문제). 그래서 비우고 → flush → 채운다.
     */
    @Transactional
    public ShowDetailResponse replaceSetlist(Long id, List<SetlistEntryRequest> entries) {
        Show show = find(id);
        show.clearSetlist();
        showRepository.flush();
        fillSetlist(show, entries);
        return ShowDetailResponse.from(show);
    }

    @Transactional
    public void delete(Long id) {
        showRepository.delete(find(id));
    }

    private Show find(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("공연 회차를 찾을 수 없습니다. id=" + id));
    }

    private void fillSetlist(Show show, List<SetlistEntryRequest> entries) {
        if (entries == null) {
            return;
        }
        int position = 1;
        for (SetlistEntryRequest entry : entries) {
            Song song = entry.songId() == null ? null : songRepository.findById(entry.songId())
                    .orElseThrow(() -> new ResourceNotFoundException("곡을 찾을 수 없습니다. id=" + entry.songId()));
            String title = entry.title() == null || entry.title().isBlank() ? null : entry.title().strip();
            if (song == null && title == null) {
                throw new IllegalArgumentException("세트리스트 " + position + "번째 항목에 songId나 title이 필요합니다.");
            }
            show.addEntry(new SetlistEntry(
                    show,
                    position++,
                    entry.entryType() == null ? SetlistEntryType.SONG : entry.entryType(),
                    song,
                    title,
                    Boolean.TRUE.equals(entry.encore()),
                    entry.memo()
            ));
        }
    }

    private List<Artist> resolveArtists(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Artist> found = artistRepository.findAllById(ids);
        if (found.size() != ids.stream().distinct().count()) {
            throw new ResourceNotFoundException("존재하지 않는 아티스트 id가 포함되어 있습니다.");
        }
        return found;
    }
}
