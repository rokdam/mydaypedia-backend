package com.mydaypedia.backend.concert;

import com.mydaypedia.backend.common.exception.ResourceNotFoundException;
import com.mydaypedia.backend.concert.dto.TourCreateRequest;
import com.mydaypedia.backend.concert.dto.TourDetailResponse;
import com.mydaypedia.backend.concert.dto.TourResponse;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** open-in-view=false라서 Tour.shows(와 각 회차의 연관관계)는 이 서비스(트랜잭션) 안에서 DTO로 바꾼다. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class TourService {

    private final TourRepository tourRepository;

    @Transactional
    public TourResponse create(TourCreateRequest request) {
        return TourResponse.from(tourRepository.save(new Tour(request.name(), request.eventType(), request.memo())));
    }

    /** 최근 투어부터 (회차가 없는 투어는 맨 뒤). */
    public List<TourResponse> list() {
        return tourRepository.findAll().stream()
                .map(TourResponse::from)
                .sorted(Comparator.comparing(TourResponse::startDate, Comparator.nullsFirst(Comparator.naturalOrder())).reversed())
                .toList();
    }

    public TourDetailResponse get(Long id) {
        return TourDetailResponse.from(find(id));
    }

    @Transactional
    public void delete(Long id) {
        Tour tour = find(id);
        if (!tour.getShows().isEmpty()) {
            throw new IllegalArgumentException("회차가 남아 있는 투어는 삭제할 수 없습니다. 회차를 먼저 삭제하세요. id=" + id);
        }
        tourRepository.delete(tour);
    }

    Tour find(Long id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("투어를 찾을 수 없습니다. id=" + id));
    }
}
