package com.mydaypedia.backend.concert;

import com.mydaypedia.backend.concert.dto.SetlistReplaceRequest;
import com.mydaypedia.backend.concert.dto.ShowCreateRequest;
import com.mydaypedia.backend.concert.dto.ShowDetailResponse;
import com.mydaypedia.backend.concert.dto.ShowSummaryResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
@Tag(name = "Shows", description = "공연 회차(날짜·장소 단위)와 세트리스트. 콘서트/팬미팅/페스티벌.")
class ShowController {

    private final ShowService showService;

    @PostMapping
    @Operation(summary = "공연 회차 등록", description = "세트리스트를 함께 넣을 수 있다. 투어가 없으면 eventType 필수(400). sourceUrl이 겹치면 409.")
    public ResponseEntity<ShowDetailResponse> create(@Valid @RequestBody ShowCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.create(request));
    }

    @GetMapping
    @Operation(
            summary = "공연 회차 목록/검색",
            description = "날짜순. tourId/eventType(CONCERT·FANMEETING·FESTIVAL)/artistId(공연한 아티스트)/year/"
                    + "songId(세트리스트에서 그 곡을 부른 회차)로 거를 수 있다(AND). 세트리스트는 개수만."
    )
    public List<ShowSummaryResponse> search(
            @RequestParam(required = false) Long tourId,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long songId
    ) {
        return showService.search(new ShowSearchCriteria(tourId, eventType, artistId, year, songId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "공연 회차 단건", description = "세트리스트 전체 포함.")
    public ShowDetailResponse get(@PathVariable Long id) {
        return showService.get(id);
    }

    @PutMapping("/{id}/setlist")
    @Operation(summary = "세트리스트 교체", description = "주어진 목록으로 통째로 바꾼다(순서 = 목록 순서). 항목마다 songId나 title 중 하나는 필수(400).")
    public ShowDetailResponse replaceSetlist(@PathVariable Long id, @Valid @RequestBody SetlistReplaceRequest request) {
        return showService.replaceSetlist(id, request.entries());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "공연 회차 삭제", description = "세트리스트도 함께 삭제된다.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        showService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
