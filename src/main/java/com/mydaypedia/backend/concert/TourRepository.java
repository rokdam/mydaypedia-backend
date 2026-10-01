package com.mydaypedia.backend.concert;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface TourRepository extends JpaRepository<Tour, Long> {

    List<Tour> findAllByOrderByNameAsc();
}
