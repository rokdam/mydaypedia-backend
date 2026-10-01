package com.mydaypedia.backend.concert.dto;

import com.mydaypedia.backend.concert.EventType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ShowCreateRequest(
        /** 소속 투어 id (선택). 없으면 단독 공연. */
        Long tourId,
        /** 생략하면 투어의 유형을 따른다. 투어가 없으면 필수. */
        EventType eventType,
        String title,
        @NotNull LocalDate showDate,
        LocalTime startTime,
        String venue,
        String city,
        String country,
        String memo,
        /** 출처 URL (선택). 이미 등록된 URL이면 409. */
        String sourceUrl,
        /** 공연한 아티스트 — /api/artists의 id 목록. */
        List<Long> artistIds,
        List<@Valid SetlistEntryRequest> setlist
) {
}
