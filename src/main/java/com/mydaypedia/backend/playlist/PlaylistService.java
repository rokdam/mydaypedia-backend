package com.mydaypedia.backend.playlist;

import com.mydaypedia.backend.common.exception.DuplicateResourceException;
import com.mydaypedia.backend.common.exception.ResourceNotFoundException;
import com.mydaypedia.backend.playlist.dto.PlaylistCreateRequest;
import com.mydaypedia.backend.playlist.dto.PlaylistResponse;
import com.mydaypedia.backend.playlist.dto.PlaylistUpdateRequest;
import com.mydaypedia.backend.song.Song;
import com.mydaypedia.backend.song.SongRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 응답 DTO 매핑은 반드시 이 서비스(트랜잭션) 안에서 끝낸다.
 * open-in-view=false이므로, 지연 로딩된 연관관계(Playlist.items, PlaylistSong.song)를
 * 트랜잭션이 끝난 뒤 컨트롤러에서 접근하면 LazyInitializationException이 발생한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongRepository songRepository;

    @Transactional
    public PlaylistResponse create(PlaylistCreateRequest request) {
        Playlist playlist = playlistRepository.save(new Playlist(request.name(), request.description()));
        return PlaylistResponse.summary(playlist);
    }

    public List<PlaylistResponse> list() {
        return playlistRepository.findAll().stream()
                .map(PlaylistResponse::summary)
                .toList();
    }

    public PlaylistResponse getDetail(Long id) {
        return PlaylistResponse.from(findDetail(id));
    }

    @Transactional
    public PlaylistResponse update(Long id, PlaylistUpdateRequest request) {
        Playlist playlist = findById(id);
        playlist.applyUpdate(request.name(), request.description());
        return PlaylistResponse.summary(playlist);
    }

    @Transactional
    public void delete(Long id) {
        if (!playlistRepository.existsById(id)) {
            throw new ResourceNotFoundException("플레이리스트를 찾을 수 없습니다. id=" + id);
        }
        playlistRepository.deleteById(id);
    }

    @Transactional
    public PlaylistResponse addSong(Long playlistId, Long songId) {
        Playlist playlist = findDetail(playlistId);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("곡을 찾을 수 없습니다. id=" + songId));

        playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId).ifPresent(existing -> {
            throw new DuplicateResourceException("이미 플레이리스트에 담긴 곡입니다. songId=" + songId);
        });

        PlaylistSong playlistSong = new PlaylistSong(playlist, song, playlist.nextPosition());
        playlist.getItems().add(playlistSong);
        return PlaylistResponse.from(playlist);
    }

    @Transactional
    public void removeSong(Long playlistId, Long songId) {
        PlaylistSong playlistSong = playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "플레이리스트에서 해당 곡을 찾을 수 없습니다. playlistId=" + playlistId + ", songId=" + songId));
        playlistSongRepository.delete(playlistSong);
    }

    private Playlist findById(Long id) {
        return playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("플레이리스트를 찾을 수 없습니다. id=" + id));
    }

    private Playlist findDetail(Long id) {
        return playlistRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("플레이리스트를 찾을 수 없습니다. id=" + id));
    }
}
