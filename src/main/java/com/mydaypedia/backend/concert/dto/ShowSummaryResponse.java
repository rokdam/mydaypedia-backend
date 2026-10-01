package com.mydaypedia.backend.concert.dto;

import com.mydaypedia.backend.artist.dto.ArtistResponse;
import com.mydaypedia.backend.concert.EventType;
import com.mydaypedia.backend.concert.Show;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** 회차 목록용 (세트리스트는 개수만). */
public record ShowSummaryResponse(
        Long id,
        Long tourId,
        String tourName,
        EventType eventType,
        String title,
        LocalDate showDate,
        LocalTime startTime,
        String venue,
        String city,
        String country,
        List<ArtistResponse> artists,
        int setlistSize
) {

    public static ShowSummaryResponse from(Show show) {
        return new ShowSummaryResponse(
                show.getId(),
                show.getTour() == null ? null : show.getTour().getId(),
                show.getTour() == null ? null : show.getTour().getName(),
                show.getEventType(),
                show.getTitle(),
                show.getShowDate(),
                show.getStartTime(),
                show.getVenue(),
                show.getCity(),
                show.getCountry(),
                show.getArtists().stream().map(ArtistResponse::from).toList(),
                show.getSetlist().size()
        );
    }
}
