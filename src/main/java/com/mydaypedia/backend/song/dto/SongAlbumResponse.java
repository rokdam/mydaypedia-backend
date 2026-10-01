package com.mydaypedia.backend.song.dto;

import com.mydaypedia.backend.album.dto.AlbumSummaryResponse;
import com.mydaypedia.backend.song.SongAlbum;

public record SongAlbumResponse(
        AlbumSummaryResponse album,
        Integer trackNumber,
        Long bugsTrackId
) {

    public static SongAlbumResponse from(SongAlbum link) {
        return new SongAlbumResponse(AlbumSummaryResponse.from(link.getAlbum()), link.getTrackNumber(), link.getBugsTrackId());
    }
}
