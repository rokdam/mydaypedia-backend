package com.mydaypedia.backend.songwriter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 작곡가/작사가 등 곡 크레딧에 들어가는 사람. 한 곡에 여러 명이 붙을 수 있고(작곡 여러 명, 작사 여러 명),
 * 한 사람이 작곡과 작사를 동시에 맡을 수도 있다 (역할은 Song 쪽 크레딧에서 관리).
 */
@Entity
@Table(name = "songwriters")
@Getter
@Setter
@NoArgsConstructor
public class Songwriter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** 벅스 아티스트 id. 벅스 크레딧에 링크가 걸린 사람만 있다(링크 없는 이름만 있는 크레딧은 null). 재수집 시 중복 방지용. */
    @Column(name = "bugs_artist_id", unique = true)
    private Long bugsArtistId;

    /**
     * 이 크레딧이 그룹 이름일 때(예: 'DAY6 (데이식스)')의 구성원. 곡 검색에서 멤버로 찾으면 그룹 크레딧 곡도 함께 나온다.
     * 개인이면 비어 있다.
     */
    @ManyToMany
    @JoinTable(name = "songwriter_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id"))
    private Set<Songwriter> members = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Songwriter(String name, Long bugsArtistId) {
        this.name = name;
        this.bugsArtistId = bugsArtistId;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void replaceMembers(List<Songwriter> wanted) {
        members.retainAll(wanted);
        members.addAll(wanted);
    }
}
