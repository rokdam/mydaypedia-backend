package com.mydaypedia.backend.playlist.dto;

import com.mydaypedia.backend.playlist.Playlist;
import java.time.LocalDateTime;
import java.util.List;

public record PlaylistResponse(
        Long id,
        String name,
        String description,
        List<PlaylistSongResponse> songs,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PlaylistResponse from(Playlist playlist) {
        return new PlaylistResponse(
                playlist.getId(),
                playlist.getName(),
                playlist.getDescription(),
                playlist.getItems().stream().map(PlaylistSongResponse::from).toList(),
                playlist.getCreatedAt(),
                playlist.getUpdatedAt()
        );
    }

    public static PlaylistResponse summary(Playlist playlist) {
        return new PlaylistResponse(
                playlist.getId(),
                playlist.getName(),
                playlist.getDescription(),
                List.of(),
                playlist.getCreatedAt(),
                playlist.getUpdatedAt()
        );
    }
}
