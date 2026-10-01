package com.mydaypedia.backend.concert;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

final class ShowSpecifications {

    private ShowSpecifications() {
    }

    static Specification<Show> tourIdEquals(Long tourId) {
        return (root, query, cb) -> tourId == null ? null : cb.equal(root.get("tour").get("id"), tourId);
    }

    static Specification<Show> eventTypeEquals(EventType eventType) {
        return (root, query, cb) -> eventType == null ? null : cb.equal(root.get("eventType"), eventType);
    }

    static Specification<Show> performedBy(Long artistId) {
        return (root, query, cb) -> {
            if (artistId == null) {
                return null;
            }
            query.distinct(true);
            return cb.equal(root.join("artists", JoinType.INNER).get("id"), artistId);
        };
    }

    static Specification<Show> yearEquals(Integer year) {
        return (root, query, cb) -> year == null
                ? null
                : cb.equal(cb.function("YEAR", Integer.class, root.get("showDate")), year);
    }

    static Specification<Show> setlistContains(Long songId) {
        return (root, query, cb) -> {
            if (songId == null) {
                return null;
            }
            query.distinct(true);
            return cb.equal(root.join("setlist", JoinType.INNER).get("song").get("id"), songId);
        };
    }
}
