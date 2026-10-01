package com.mydaypedia.backend.concert;

/** 세트리스트 항목 종류. 곡 통계(많이 부른 곡 등)에서 VCR/토크 같은 곡이 아닌 항목을 거르기 위해 나눈다. */
public enum SetlistEntryType {
    /** 부른 곡. songs에 있으면 곡으로 연결하고, 커버곡·미발매곡처럼 없으면 제목만 텍스트로 둔다. */
    SONG,
    /** VCR, 토크, 인트로/아웃트로 등 곡이 아닌 순서. */
    OTHER
}
