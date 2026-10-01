package com.mydaypedia.backend.playlist;

import com.mydaypedia.backend.playlist.dto.AddSongRequest;
import com.mydaypedia.backend.playlist.dto.PlaylistCreateRequest;
import com.mydaypedia.backend.playlist.dto.PlaylistResponse;
import com.mydaypedia.backend.playlist.dto.PlaylistUpdateRequest;
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
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@Tag(name = "Playlists", description = "취향대로 담는 플레이리스트")
class PlaylistController {

    private final PlaylistService playlistService;

    @PostMapping
    @Operation(summary = "플레이리스트 생성")
    public ResponseEntity<PlaylistResponse> create(@Valid @RequestBody PlaylistCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playlistService.create(request));
    }

    @GetMapping
    @Operation(summary = "플레이리스트 목록 조회")
    public List<PlaylistResponse> list() {
        return playlistService.list();
    }

    @GetMapping("/{id}")
    @Operation(summary = "플레이리스트 단건 조회", description = "담긴 곡 목록까지 함께 반환한다.")
    public PlaylistResponse get(@PathVariable Long id) {
        return playlistService.getDetail(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "플레이리스트 이름/설명 수정")
    public PlaylistResponse update(@PathVariable Long id, @Valid @RequestBody PlaylistUpdateRequest request) {
        return playlistService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "플레이리스트 삭제")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playlistService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/songs")
    @Operation(summary = "플레이리스트에 곡 추가")
    public ResponseEntity<PlaylistResponse> addSong(@PathVariable Long id, @Valid @RequestBody AddSongRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playlistService.addSong(id, request.songId()));
    }

    @DeleteMapping("/{id}/songs/{songId}")
    @Operation(summary = "플레이리스트에서 곡 제거")
    public ResponseEntity<Void> removeSong(@PathVariable Long id, @PathVariable Long songId) {
        playlistService.removeSong(id, songId);
        return ResponseEntity.noContent().build();
    }
}
