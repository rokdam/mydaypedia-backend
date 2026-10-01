package com.mydaypedia.backend.concert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 여러 공연 회차를 묶는 투어 (예: 3RD WORLD TOUR 'FOREVER YOUNG'). 페스티벌·단발 공연은 투어 없이 회차(Show)만 둔다.
 * 투어 기간은 따로 저장하지 않고 회차 날짜에서 계산한다 (회차를 추가/삭제해도 어긋나지 않게).
 */
@Entity
@Table(name = "tours")
@Getter
@Setter
@NoArgsConstructor
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    @Column(length = 1000)
    private String memo;

    @OneToMany(mappedBy = "tour")
    @OrderBy("showDate ASC, startTime ASC")
    private List<Show> shows = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Tour(String name, EventType eventType, String memo) {
        this.name = name;
        this.eventType = eventType;
        this.memo = memo;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public LocalDate getStartDate() {
        return shows.stream().map(Show::getShowDate).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
    }

    public LocalDate getEndDate() {
        return shows.stream().map(Show::getShowDate).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
    }
}
