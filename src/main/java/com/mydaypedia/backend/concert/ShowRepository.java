package com.mydaypedia.backend.concert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface ShowRepository extends JpaRepository<Show, Long>, JpaSpecificationExecutor<Show> {
}
