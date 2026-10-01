package com.mydaypedia.backend.concert.dto;

import com.mydaypedia.backend.concert.SetlistEntry;
import com.mydaypedia.backend.concert.SetlistEntryType;

public record SetlistEntryResponse(
        Integer position,
        SetlistEntryType entryType,
        /** 연결된 곡 id. 커버곡·VCR 등 곡 DB에 없으면 null. */
        Long songId,
        /** 표시용 제목 (출처 표기가 있으면 그대로, 없으면 곡 제목). */
        String title,
        boolean encore,
        String memo
) {

    public static SetlistEntryResponse from(SetlistEntry entry) {
        return new SetlistEntryResponse(
                entry.getPosition(),
                entry.getEntryType(),
                entry.getSong() == null ? null : entry.getSong().getId(),
                entry.getDisplayTitle(),
                entry.isEncore(),
                entry.getMemo()
        );
    }
}
