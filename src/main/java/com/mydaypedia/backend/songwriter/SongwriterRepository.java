package com.mydaypedia.backend.songwriter;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongwriterRepository extends JpaRepository<Songwriter, Long> {

    List<Songwriter> findAllByOrderByNameAsc();
}
