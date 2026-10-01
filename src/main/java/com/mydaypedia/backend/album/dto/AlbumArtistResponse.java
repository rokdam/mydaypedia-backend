package com.mydaypedia.backend.album.dto;

import com.mydaypedia.backend.album.AlbumArtist;
import com.mydaypedia.backend.album.AlbumArtistRole;

public record AlbumArtistResponse(
        Long artistId,
        String name,
        AlbumArtistRole role
) {

    public static AlbumArtistResponse from(AlbumArtist link) {
        return new AlbumArtistResponse(link.getArtist().getId(), link.getArtist().getName(), link.getRole());
    }
}
