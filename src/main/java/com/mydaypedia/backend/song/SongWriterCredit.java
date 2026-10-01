package com.mydaypedia.backend.song;

import com.mydaypedia.backend.songwriter.Songwriter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Song과 Songwriter를 역할(작곡/작사)과 함께 잇는 크레딧. 한 곡에 역할별로 여러 명이 붙을 수 있다. */
@Entity
@Table(name = "song_writer_credits",
        uniqueConstraints = @UniqueConstraint(columnNames = {"song_id", "songwriter_id", "role"}))
@Getter
@NoArgsConstructor
class SongWriterCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "songwriter_id", nullable = false)
    private Songwriter songwriter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SongWriterRole role;

    SongWriterCredit(Song song, Songwriter songwriter, SongWriterRole role) {
        this.song = song;
        this.songwriter = songwriter;
        this.role = role;
    }
}
