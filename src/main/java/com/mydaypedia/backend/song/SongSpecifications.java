package com.mydaypedia.backend.song;

import com.mydaypedia.backend.album.AlbumType;
import com.mydaypedia.backend.songwriter.Songwriter;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

final class SongSpecifications {

    private SongSpecifications() {
    }

    static Specification<Song> titleContains(String title) {
        return (root, query, cb) -> title == null || title.isBlank()
                ? null
                : cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    /** 수록 앨범 중 하나라도 제목이 맞으면 포함. 여러 앨범에 실린 곡이 중복으로 나오지 않게 distinct. */
    static Specification<Song> albumTitleContains(String album) {
        return (root, query, cb) -> {
            if (album == null || album.isBlank()) {
                return null;
            }
            query.distinct(true);
            Join<Song, SongAlbum> link = root.join("albumLinks", JoinType.INNER);
            return cb.like(cb.lower(link.get("album").get("title")), "%" + album.toLowerCase() + "%");
        };
    }

    static Specification<Song> albumIdEquals(Long albumId) {
        return (root, query, cb) -> {
            if (albumId == null) {
                return null;
            }
            query.distinct(true);
            Join<Song, SongAlbum> link = root.join("albumLinks", JoinType.INNER);
            return cb.equal(link.get("album").get("id"), albumId);
        };
    }

    /** 수록 앨범 중 하나라도 그 유형이면 포함 (예: 싱글로 먼저 나오고 정규에 재수록된 곡은 SINGLE·REGULAR 둘 다). */
    static Specification<Song> albumTypeEquals(AlbumType albumType) {
        return (root, query, cb) -> {
            if (albumType == null) {
                return null;
            }
            query.distinct(true);
            Join<Song, SongAlbum> link = root.join("albumLinks", JoinType.INNER);
            return cb.equal(link.get("album").get("albumType"), albumType);
        };
    }

    static Specification<Song> sungBy(Long artistId) {
        return (root, query, cb) -> {
            if (artistId == null) {
                return null;
            }
            query.distinct(true);
            return cb.equal(root.join("artists", JoinType.INNER).get("id"), artistId);
        };
    }

    /** 가나다 색인: 'ㄱ'(쌍자음 ㄲ 포함), 'A', '#'(숫자·기타) — Song.titleInitial과 정확 일치 */
    static Specification<Song> initialEquals(String initial) {
        return (root, query, cb) -> initial == null || initial.isBlank()
                ? null
                : cb.equal(root.get("titleInitial"), initial.trim().toUpperCase());
    }

    /** 수록 앨범 중 하나라도 이 트랙 번호면 포함 (예: 1 → 각 앨범의 1번 트랙) */
    static Specification<Song> trackNumberEquals(Integer trackNumber) {
        return (root, query, cb) -> {
            if (trackNumber == null) {
                return null;
            }
            query.distinct(true);
            Join<Song, SongAlbum> link = root.join("albumLinks", JoinType.INNER);
            return cb.equal(link.get("trackNumber"), trackNumber);
        };
    }

    /**
     * 장르 하나로 거른다. 한 곡에 장르가 여럿이면 "댄스/팝, 인디, 락/메탈"처럼 ", "로 이어 저장하므로,
     * 앞뒤에 구분자를 붙여 ", 인디," 같은 **통째 항목**으로 찾는다 — 그냥 부분일치하면 "팝"이 "댄스/팝"에도 걸린다.
     */
    static Specification<Song> genreEquals(String genre) {
        return (root, query, cb) -> {
            if (genre == null || genre.isBlank()) {
                return null;
            }
            Expression<String> wrapped = cb.concat(cb.concat(", ", cb.lower(root.get("genre"))), ",");
            return cb.like(wrapped, "%, " + genre.trim().toLowerCase() + ",%");
        };
    }

    static Specification<Song> songTypeEquals(SongType songType) {
        return (root, query, cb) -> songType == null
                ? null
                : cb.equal(root.get("songType"), songType);
    }

    static Specification<Song> hasFeaturing(Boolean hasFeaturing) {
        return (root, query, cb) -> hasFeaturing == null
                ? null
                : hasFeaturing
                        ? cb.isNotNull(root.get("featuredArtist"))
                        : cb.isNull(root.get("featuredArtist"));
    }

    /**
     * 여러 작곡가/작사가 조합 검색.
     * ANY: 고른 사람 중 한 명이라도 참여한 곡 (크레딧 join 하나에 IN 조건).
     * ALL: 고른 사람이 전부 참여한 곡 (사람마다 크레딧 join을 따로 걸어 AND — 같은 크레딧 행 하나로는 여러 사람을 동시에 만족할 수 없으므로).
     */
    static Specification<Song> writtenBy(List<Long> songwriterIds, CreditMatch match, SongWriterRole role) {
        if (songwriterIds == null || songwriterIds.isEmpty()) {
            return (root, query, cb) -> null;
        }
        List<Long> ids = songwriterIds.stream().distinct().toList();
        Specification<Song> all = Specification.allOf(ids.stream().map(id -> creditedToAny(List.of(id), role)).toList());
        return switch (match == null ? CreditMatch.ANY : match) {
            case ANY -> creditedToAny(ids, role);
            case ALL -> all;
            case EXACT -> all.and(noOtherMembers(ids, role));
        };
    }

    /**
     * 고른 사람들과 같은 그룹에 속한 "다른 멤버"나 그 그룹 크레딧 자체가 같은 역할로 붙은 곡을 뺀다.
     * 그룹에 속하지 않은 외부 작곡가는 영향을 주지 않는다.
     */
    private static Specification<Song> noOtherMembers(List<Long> songwriterIds, SongWriterRole role) {
        return (root, query, cb) -> {
            // 고른 사람이 속한 그룹들
            Subquery<Long> groups = query.subquery(Long.class);
            Root<Songwriter> group = groups.from(Songwriter.class);
            groups.select(group.get("id")).where(group.join("members").get("id").in(songwriterIds));

            // 그 그룹들의 구성원 중 고르지 않은 사람
            Subquery<Long> otherMembers = query.subquery(Long.class);
            Root<Songwriter> sameGroup = otherMembers.from(Songwriter.class);
            Join<Songwriter, Songwriter> member = sameGroup.join("members");
            otherMembers.select(member.get("id")).where(
                    sameGroup.get("id").in(groups),
                    cb.not(member.get("id").in(songwriterIds)));

            Subquery<Long> conflicting = query.subquery(Long.class);
            Root<SongWriterCredit> other = conflicting.from(SongWriterCredit.class);
            Path<Long> otherWriterId = other.get("songwriter").get("id");
            conflicting.select(other.get("id")).where(
                    cb.equal(other.get("song"), root),
                    cb.equal(other.get("role"), role),
                    cb.or(otherWriterId.in(otherMembers), otherWriterId.in(groups)));

            return cb.not(cb.exists(conflicting));
        };
    }

    /**
     * 주어진 사람들 중 누군가의 본인 크레딧 + 그 사람이 구성원인 그룹 크레딧(예: 'DAY6 (데이식스)' 작곡)까지 포함한다.
     * 그룹 여부는 Songwriter.members(songwriter_members)로 판단한다.
     */
    private static Specification<Song> creditedToAny(List<Long> songwriterIds, SongWriterRole role) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Song, SongWriterCredit> credit = root.join("writerCredits", JoinType.INNER);
            Path<Long> writerId = credit.get("songwriter").get("id");

            Subquery<Long> groupsOfWriters = query.subquery(Long.class);
            Root<Songwriter> group = groupsOfWriters.from(Songwriter.class);
            groupsOfWriters.select(group.get("id"))
                    .where(group.join("members").get("id").in(songwriterIds));

            return cb.and(
                    cb.or(writerId.in(songwriterIds), writerId.in(groupsOfWriters)),
                    cb.equal(credit.get("role"), role)
            );
        };
    }

    static Specification<Song> releaseYearEquals(Integer year) {
        return (root, query, cb) -> year == null
                ? null
                : cb.equal(cb.function("YEAR", Integer.class, root.get("releaseDate")), year);
    }

    static Specification<Song> releaseMonthEquals(Integer month) {
        return (root, query, cb) -> month == null
                ? null
                : cb.equal(cb.function("MONTH", Integer.class, root.get("releaseDate")), month);
    }

    static Specification<Song> releaseDayEquals(Integer day) {
        return (root, query, cb) -> day == null
                ? null
                : cb.equal(cb.function("DAY", Integer.class, root.get("releaseDate")), day);
    }
}
