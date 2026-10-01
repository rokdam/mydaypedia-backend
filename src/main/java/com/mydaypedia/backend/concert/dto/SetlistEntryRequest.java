package com.mydaypedia.backend.concert.dto;

import com.mydaypedia.backend.concert.SetlistEntryType;

/**
 * 세트리스트 한 순서. 순서(position)는 목록 순서로 매긴다.
 * songId나 title 중 하나는 있어야 한다. entryType 생략 시 SONG.
 */
public record SetlistEntryRequest(
        SetlistEntryType entryType,
        /** 기존 /api/songs의 id (선택). */
        Long songId,
        /** 출처 표기 그대로의 제목. 곡이 없으면(커버곡·VCR 등) 필수. */
        String title,
        Boolean encore,
        String memo
) {
}
