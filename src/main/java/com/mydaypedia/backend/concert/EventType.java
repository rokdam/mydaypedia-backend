package com.mydaypedia.backend.concert;

/**
 * 공연 유형. DB에는 VARCHAR로 저장한다(@JdbcTypeCode) — MySQL ENUM 컬럼으로 만들어지면
 * ddl-auto: update가 새 값을 추가해 주지 않아서, 값을 늘릴 때마다 수동 ALTER가 필요해진다.
 */
public enum EventType {
    CONCERT,
    FANMEETING,
    /** 페스티벌 및 참여 공연·행사 (KCON, 라이브 클럽 데이, 다른 가수 공연 게스트 등 포함) */
    FESTIVAL,
    /** 시상식·연말 가요제 */
    AWARD,
    /** 대학 축제 */
    UNIVERSITY_FESTIVAL,
    /** 쇼케이스·청음회·앨범 발매 기념 라이브 */
    SHOWCASE
}
