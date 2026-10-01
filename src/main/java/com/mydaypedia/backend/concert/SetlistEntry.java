package com.mydaypedia.backend.concert;

import com.mydaypedia.backend.song.Song;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 세트리스트의 한 순서. songs에 있는 곡이면 song으로 연결하고, 커버곡·미발매곡·VCR·토크처럼 없으면 title만 둔다.
 * title은 출처에 적힌 그대로의 표기(예: 'Congratulations (Acoustic)')라서, 곡이 연결돼 있어도 남겨 둔다.
 */
@Entity
@Table(name = "setlist_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"show_id", "position"}))
@Getter
@NoArgsConstructor
public class SetlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    /** 1부터 시작하는 순서. */
    @Column(nullable = false)
    private Integer position;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "entry_type", nullable = false, length = 20)
    private SetlistEntryType entryType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    private Song song;

    @Column(length = 200)
    private String title;

    @Column(nullable = false)
    private boolean encore;

    /** 예: 'Acoustic ver.', 'Cover of 김광석', '영케이 솔로 무대'. */
    @Column(length = 500)
    private String memo;

    SetlistEntry(Show show, Integer position, SetlistEntryType entryType, Song song, String title, boolean encore, String memo) {
        this.show = show;
        this.position = position;
        this.entryType = entryType;
        this.song = song;
        this.title = title;
        this.encore = encore;
        this.memo = memo;
    }

    /** 화면 표시용 제목: 출처 표기가 있으면 그대로, 없으면 연결된 곡 제목. */
    public String getDisplayTitle() {
        return title != null ? title : (song != null ? song.getTitle() : null);
    }
}
