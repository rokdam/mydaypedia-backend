package com.mydaypedia.backend.concert.dto;

import com.mydaypedia.backend.concert.EventType;
import com.mydaypedia.backend.concert.Tour;
import java.time.LocalDate;

/** 투어 목록용. 기간(startDate~endDate)은 회차 날짜에서 계산한 값이다. */
public record TourResponse(
        Long id,
        String name,
        EventType eventType,
        String memo,
        LocalDate startDate,
        LocalDate endDate,
        int showCount
) {

    public static TourResponse from(Tour tour) {
        return new TourResponse(tour.getId(), tour.getName(), tour.getEventType(), tour.getMemo(),
                tour.getStartDate(), tour.getEndDate(), tour.getShows().size());
    }
}
