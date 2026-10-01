package com.mydaypedia.backend.song;

import com.mydaypedia.backend.album.AlbumType;
import java.util.List;

/**
 * 곡 검색 조건. 컨트롤러의 개별 쿼리 파라미터를 서비스로 묶어 전달하기 위한 내부 객체.
 * 전부 선택 값이며, null인 조건은 검색에서 무시된다.
 * initial은 제목 색인 글자(ㄱ~ㅎ, A~Z, #), trackNumber는 수록 앨범 중 하나라도 그 트랙 번호인 곡.
 * album은 수록 앨범 제목 부분일치, albumId는 /api/albums의 id, albumType은 수록 앨범 중 하나라도 그 유형이면, artistId는 부른 아티스트(/api/artists의 id)로 찾는다.
 * composerIds/lyricistIds는 이름 텍스트 검색이 아니라 songwriter id(여러 명 가능)로 찾고,
 * composerMatch/lyricistMatch(ANY: 한 명이라도, ALL: 전부 같이)로 조합 방식을 정한다
 * (프론트에서 /api/songwriters 목록을 보여주고 클릭해서 고르는 방식을 전제로 한다).
 */
record SongSearchCriteria(
        String title,
        String initial,
        Integer trackNumber,
        String album,
        Long albumId,
        AlbumType albumType,
        Long artistId,
        String genre,
        List<Long> composerIds,
        CreditMatch composerMatch,
        List<Long> lyricistIds,
        CreditMatch lyricistMatch,
        SongType songType,
        Boolean hasFeaturing,
        Integer year,
        Integer month,
        Integer day
) {
}
