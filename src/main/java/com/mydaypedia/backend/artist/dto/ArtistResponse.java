package com.mydaypedia.backend.artist.dto;

import com.mydaypedia.backend.artist.Artist;

public record ArtistResponse(
        Long id,
        String name,
        Long bugsArtistId
) {

    public static ArtistResponse from(Artist artist) {
        return new ArtistResponse(artist.getId(), artist.getName(), artist.getBugsArtistId());
    }
}
