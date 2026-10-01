package com.mydaypedia.backend.song;

import com.mydaypedia.backend.album.AlbumType;
import com.mydaypedia.backend.song.dto.SongAlbumRequest;
import com.mydaypedia.backend.song.dto.SongCreateRequest;
import com.mydaypedia.backend.song.dto.SongMelonIdRequest;
import com.mydaypedia.backend.song.dto.SongResponse;
import com.mydaypedia.backend.song.dto.SongUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Tag(name = "Songs", description = "DAY6 곡 아카이브")
class SongController {

    private final SongService songService;

    @PostMapping
    @Operation(summary = "곡 등록")
    public ResponseEntity<SongResponse> create(@Valid @RequestBody SongCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(songService.create(request));
    }

    @GetMapping
    @Operation(
            summary = "곡 검색/목록 조회",
            description = "title/initial(제목 색인 글자: ㄱ~ㅎ(쌍자음은 기본 자음에 포함)·A~Z·#)/trackNumber(수록 앨범 중 하나라도 그 트랙 번호)/album(수록 앨범 제목)/albumId/albumType(수록 앨범 유형 REGULAR·EP·SINGLE·OST·COMPILATION)/artistId(부른 아티스트)/genre(장르 하나 — 여러 장르가 붙은 곡도 그 장르가 들어 있으면 포함)/작곡가·작사가(id)/피처링 여부/OST·CM송 구분/연도·월·일로 취향에 맞는 곡을 찾을 수 있다. "
                    + "composerIds/lyricistIds는 /api/songwriters에 등록된 id를 넘긴다(이름 검색이 아니라, 저장된 목록에서 클릭해서 고르는 방식을 전제). "
                    + "여러 명은 composerIds=16&composerIds=13 또는 composerIds=16,13. "
                    + "composerMatch/lyricistMatch로 조합 방식: ANY(기본, 한 명이라도 참여한 곡) / ALL(전부 같이 참여한 곡) / EXACT(전부 같이 참여 + 같은 그룹의 다른 멤버는 불참, 외부 작곡가는 허용). "
                    + "멤버를 고르면 그 멤버가 속한 그룹 크레딧(예: 'DAY6 (데이식스)') 곡도 포함된다. "
                    + "조건은 전부 선택이며 동시에 여러 개를 조합할 수 있다(AND). 파라미터가 없으면 전체 목록을 반환한다."
    )
    public List<SongResponse> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String initial,
            @RequestParam(required = false) Integer trackNumber,
            @RequestParam(required = false) String album,
            @RequestParam(required = false) Long albumId,
            @RequestParam(required = false) AlbumType albumType,
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) List<Long> composerIds,
            @RequestParam(defaultValue = "ANY") CreditMatch composerMatch,
            @RequestParam(required = false) List<Long> lyricistIds,
            @RequestParam(defaultValue = "ANY") CreditMatch lyricistMatch,
            @RequestParam(required = false) SongType songType,
            @RequestParam(required = false) Boolean hasFeaturing,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day
    ) {
        SongSearchCriteria criteria = new SongSearchCriteria(
                title, initial, trackNumber, album, albumId, albumType, artistId, genre, composerIds, composerMatch, lyricistIds, lyricistMatch,
                songType, hasFeaturing, year, month, day);
        return songService.search(criteria);
    }

    @GetMapping("/{id}")
    @Operation(summary = "곡 단건 조회")
    public SongResponse get(@PathVariable Long id) {
        return songService.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "곡 수정")
    public SongResponse update(@PathVariable Long id, @Valid @RequestBody SongUpdateRequest request) {
        return songService.update(id, request);
    }

    @PostMapping("/{id}/albums")
    @Operation(summary = "곡에 수록 앨범 추가", description = "이미 그 앨범에 수록되어 있으면 409, bugsTrackId가 다른 수록 정보와 겹쳐도 409.")
    public ResponseEntity<SongResponse> addAlbum(@PathVariable Long id, @Valid @RequestBody SongAlbumRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(songService.addAlbum(id, request));
    }

    @PutMapping("/{id}/melon-id")
    @Operation(summary = "곡의 멜론 곡 번호 지정", description = "melonSongId가 null이면 지운다. 멜론 원클릭 재생 링크에 쓴다.")
    public SongResponse updateMelonSongId(@PathVariable Long id, @Valid @RequestBody SongMelonIdRequest request) {
        return songService.updateMelonSongId(id, request.melonSongId());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "곡 삭제")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        songService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
