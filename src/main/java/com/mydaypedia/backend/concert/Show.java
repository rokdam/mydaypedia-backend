package com.mydaypedia.backend.concert;

import com.mydaypedia.backend.artist.Artist;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** 공연 회차 하나 (날짜·장소 단위). 투어에 속하거나(콘서트/팬미팅 투어), 투어 없이 단독(페스티벌 출연 등)일 수 있다. */
@Entity
@Table(name = "shows")
@Getter
@Setter
@NoArgsConstructor
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소속 투어. null이면 단독 공연(페스티벌 출연 등). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    /** 회차 자체의 이름. 페스티벌명(예: 인천펜타포트 락 페스티벌 2023)이나 투어 없는 공연명. 투어 회차면 보통 비운다. */
    @Column(length = 200)
    private String title;

    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    /** 개최 장소 (예: KSPO DOME). */
    @Column(length = 200)
    private String venue;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(length = 1000)
    private String memo;

    /** 출처 페이지 URL. 출처 추적 + 수집 스크립트 재실행 시 중복 방지용. */
    @Column(name = "source_url", length = 500, unique = true)
    private String sourceUrl;

    /** 공연한 추적 아티스트 (DAY6, 멤버 솔로, 유닛 등). */
    @ManyToMany
    @JoinTable(name = "show_artists",
            joinColumns = @JoinColumn(name = "show_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id"))
    private Set<Artist> artists = new LinkedHashSet<>();

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<SetlistEntry> setlist = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void replaceArtists(List<Artist> wanted) {
        artists.retainAll(wanted);
        artists.addAll(wanted);
    }

    /** 세트리스트를 비운다. 새 항목을 넣기 전에 반드시 flush해야 한다(ShowService 참고). */
    void clearSetlist() {
        setlist.clear();
    }

    void addEntry(SetlistEntry entry) {
        setlist.add(entry);
    }
}
