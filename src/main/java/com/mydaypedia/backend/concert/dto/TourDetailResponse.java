package com.mydaypedia.backend.concert.dto;

import com.mydaypedia.backend.concert.Tour;
import java.util.List;

/** 투어 단건: 투어 정보 + 회차 목록(날짜순, 세트리스트 제외). */
public record TourDetailResponse(
        TourResponse tour,
        List<ShowSummaryResponse> shows
) {

    public static TourDetailResponse from(Tour tour) {
        return new TourDetailResponse(TourResponse.from(tour), tour.getShows().stream().map(ShowSummaryResponse::from).toList());
    }
}
