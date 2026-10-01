package com.mydaypedia.backend.playlist.dto;

import com.mydaypedia.backend.playlist.PlaylistSong;
import com.mydaypedia.backend.song.dto.SongResponse;
import java.time.LocalDateTime;

public record PlaylistSongResponse(
        int position,
        LocalDateTime addedAt,
        SongResponse song
) {

    public static PlaylistSongResponse from(PlaylistSong playlistSong) {
        return new PlaylistSongResponse(
                playlistSong.getPosition(),
                playlistSong.getAddedAt(),
                SongResponse.from(playlistSong.getSong())
        );
    }
}
