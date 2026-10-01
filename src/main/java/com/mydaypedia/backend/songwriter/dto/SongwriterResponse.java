package com.mydaypedia.backend.songwriter.dto;

import com.mydaypedia.backend.songwriter.Songwriter;

public record SongwriterResponse(
        Long id,
        String name,
        Long bugsArtistId
) {

    public static SongwriterResponse from(Songwriter songwriter) {
        return new SongwriterResponse(songwriter.getId(), songwriter.getName(), songwriter.getBugsArtistId());
    }
}
