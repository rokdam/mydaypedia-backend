package com.mydaypedia.backend.concert;

import com.mydaypedia.backend.concert.dto.TourCreateRequest;
import com.mydaypedia.backend.concert.dto.TourDetailResponse;
import com.mydaypedia.backend.concert.dto.TourResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
@Tag(name = "Tours", description = "콘서트/팬미팅 투어. 회차(날짜·장소)는 /api/shows에서 tourId로 묶는다.")
class TourController {

    private final TourService tourService;

    @PostMapping
    @Operation(summary = "투어 등록")
    public ResponseEntity<TourResponse> create(@Valid @RequestBody TourCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tourService.create(request));
    }

    @GetMapping
    @Operation(summary = "투어 목록", description = "최근 투어부터. 기간(startDate~endDate)과 회차 수는 회차에서 계산한 값.")
    public List<TourResponse> list() {
        return tourService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "투어 단건", description = "투어 정보 + 회차 목록(날짜순, 세트리스트 제외).")
    public TourDetailResponse get(@PathVariable Long id) {
        return tourService.get(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "투어 삭제", description = "회차가 남아 있으면 400.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tourService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
