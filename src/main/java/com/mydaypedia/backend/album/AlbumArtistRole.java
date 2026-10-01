package com.mydaypedia.backend.album;

/** 아티스트가 앨범에 어떤 관계로 연결되는지. 벅스 아티스트 앨범 목록의 탭(발매/참여/컴필레이션)을 따른다. */
public enum AlbumArtistRole {
    /** 본인 발매작 (벅스 `발매` 탭) */
    RELEASE,
    /** 다른 앨범에 작곡/작사/피처링 등으로 참여 (벅스 `참여` 탭) */
    JOIN,
    /** 컴필레이션 수록 (벅스 `컴필레이션` 탭) */
    COMPILATION
}
