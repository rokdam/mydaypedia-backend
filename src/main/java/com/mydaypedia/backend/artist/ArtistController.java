package com.mydaypedia.backend.artist;

import com.mydaypedia.backend.artist.dto.ArtistCreateRequest;
import com.mydaypedia.backend.artist.dto.ArtistResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
@Tag(name = "Artists", description = "추적하는 아티스트(DAY6, 멤버 솔로, 유닛). 앨범과의 관계는 /api/albums/{id}/artists로 연결한다.")
class ArtistController {

    private final ArtistService artistService;

    @PostMapping
    @Operation(summary = "아티스트 등록", description = "bugsArtistId가 이미 등록된 아티스트와 겹치면 409가 반환된다.")
    public ResponseEntity<ArtistResponse> create(@Valid @RequestBody ArtistCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(artistService.create(request));
    }

    @GetMapping
    @Operation(summary = "아티스트 전체 목록", description = "이름순. 앨범 목록 필터(artistId)용 클릭 목록으로 쓴다.")
    public List<ArtistResponse> list() {
        return artistService.list();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "아티스트 삭제", description = "앨범에 연결되어 있으면 409가 반환된다.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        artistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
