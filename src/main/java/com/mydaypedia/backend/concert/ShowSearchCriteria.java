package com.mydaypedia.backend.concert;

/** 회차 목록 검색 조건. 전부 선택 값이며 null이면 무시(AND 조합). */
record ShowSearchCriteria(
        Long tourId,
        EventType eventType,
        Long artistId,
        Integer year,
        /** 이 곡을 세트리스트에서 부른 회차만. */
        Long songId
) {
}
