package com.mydaypedia.backend.playlist;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    @Query("SELECT DISTINCT p FROM Playlist p LEFT JOIN FETCH p.items i LEFT JOIN FETCH i.song WHERE p.id = :id")
    Optional<Playlist> findDetailById(@Param("id") Long id);
}
