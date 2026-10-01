package com.mydaypedia.backend.songwriter;

import com.mydaypedia.backend.songwriter.dto.SongwriterCreateRequest;
import com.mydaypedia.backend.songwriter.dto.SongwriterGroupResponse;
import com.mydaypedia.backend.songwriter.dto.SongwriterMembersRequest;
import com.mydaypedia.backend.songwriter.dto.SongwriterResponse;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/songwriters")
@RequiredArgsConstructor
@Tag(name = "Songwriters", description = "작곡가/작사가. 곡 등록 시 이름 대신 여기서 만든 id를 참조한다 "
        + "(프론트에서는 저장된 목록을 보여주고 클릭해서 고르는 방식으로 쓰는 걸 전제로 한다).")
class SongwriterController {

    private final SongwriterService songwriterService;

    @PostMapping
    @Operation(summary = "작곡가/작사가 등록", description = "bugsArtistId가 이미 등록된 사람과 겹치면 409가 반환된다.")
    public ResponseEntity<SongwriterResponse> create(@Valid @RequestBody SongwriterCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(songwriterService.create(request));
    }

    @GetMapping
    @Operation(summary = "작곡가/작사가 전체 목록", description = "이름 가나다순. 곡 검색/등록 화면에서 클릭용 목록으로 쓴다.")
    public List<SongwriterResponse> list() {
        return songwriterService.list();
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "그룹 크레딧의 구성원 조회", description = "개인이면 members가 빈 목록이다.")
    public SongwriterGroupResponse getMembers(@PathVariable Long id) {
        return songwriterService.getMembers(id);
    }

    @PutMapping("/{id}/members")
    @Operation(
            summary = "그룹 크레딧의 구성원 지정",
            description = "예: 'DAY6 (데이식스)' 크레딧에 멤버들을 지정하면, 곡 검색에서 멤버의 composerId/lyricistId로 찾을 때 "
                    + "그룹 크레딧 곡도 함께 나온다. 주어진 목록으로 통째로 교체한다(빈 목록이면 해제). 자기 자신은 400."
    )
    public SongwriterGroupResponse replaceMembers(@PathVariable Long id, @Valid @RequestBody SongwriterMembersRequest request) {
        return songwriterService.replaceMembers(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "작곡가/작사가 삭제", description = "곡에 크레딧으로 걸려 있거나 그룹 구성원 관계가 있으면 409가 반환된다.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        songwriterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
