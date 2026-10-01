package com.mydaypedia.backend.concert.dto;

import com.mydaypedia.backend.concert.Show;
import java.util.List;

/** 회차 단건: 목록 정보 + 메모/출처 + 세트리스트 전체. */
public record ShowDetailResponse(
        ShowSummaryResponse show,
        String memo,
        String sourceUrl,
        List<SetlistEntryResponse> setlist
) {

    public static ShowDetailResponse from(Show show) {
        return new ShowDetailResponse(
                ShowSummaryResponse.from(show),
                show.getMemo(),
                show.getSourceUrl(),
                show.getSetlist().stream().map(SetlistEntryResponse::from).toList()
        );
    }
}
