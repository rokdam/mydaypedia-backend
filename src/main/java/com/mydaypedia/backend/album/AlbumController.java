package com.mydaypedia.backend.album;

import com.mydaypedia.backend.album.dto.AlbumArtistRequest;
import com.mydaypedia.backend.album.dto.AlbumCreateRequest;
import com.mydaypedia.backend.album.dto.AlbumResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
@Tag(name = "Albums", description = "앨범. 곡 등록 시 앨범명 대신 여기서 만든 id(albumId)를 참조한다.")
class AlbumController {

    private final AlbumService albumService;

    @PostMapping
    @Operation(summary = "앨범 등록", description = "bugsAlbumId가 이미 등록된 앨범과 겹치면 409가 반환된다.")
    public ResponseEntity<AlbumResponse> create(@Valid @RequestBody AlbumCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(albumService.create(request));
    }

    @GetMapping
    @Operation(
            summary = "앨범 목록",
            description = "발매일순(같으면 제목순). artistId를 주면 그 아티스트와 연결된 앨범만, "
                    + "role(RELEASE/JOIN/COMPILATION)까지 주면 그 관계로 연결된 앨범만 반환한다 "
                    + "(예: artistId=Young K, role=RELEASE → 영케이 솔로 발매작). role은 artistId와 함께 쓸 때만 적용된다."
    )
    public List<AlbumResponse> list(
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) AlbumArtistRole role
    ) {
        return albumService.list(artistId, role);
    }

    @PostMapping("/{id}/artists")
    @Operation(summary = "앨범에 아티스트 연결", description = "이미 연결된 아티스트면 409가 반환된다.")
    public ResponseEntity<AlbumResponse> addArtist(@PathVariable Long id, @Valid @RequestBody AlbumArtistRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(albumService.addArtist(id, request));
    }

    @DeleteMapping("/{id}/artists/{artistId}")
    @Operation(summary = "앨범-아티스트 연결 해제")
    public ResponseEntity<Void> removeArtist(@PathVariable Long id, @PathVariable Long artistId) {
        albumService.removeArtist(id, artistId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "앨범 삭제", description = "수록곡이 하나라도 연결되어 있으면 409가 반환된다. 아티스트 연결은 함께 삭제된다.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        albumService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
