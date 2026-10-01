package com.mydaypedia.backend.playlist;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    Optional<PlaylistSong> findByPlaylistIdAndSongId(Long playlistId, Long songId);
}
