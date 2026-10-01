package com.mydaypedia.backend.song;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * title_initial 컬럼을 추가하기 전에 저장된 곡의 색인 글자를 기동 시 채운다.
 * 새 곡/수정 곡은 Song의 @PrePersist/@PreUpdate가 채우므로, 채울 게 없으면 아무 일도 하지 않는다.
 * (마이그레이션 도구가 없어서 ddl-auto: update로 컬럼만 생기고 기존 행은 null로 남기 때문)
 */
@Slf4j
@Component
@RequiredArgsConstructor
class TitleInitialBackfill implements ApplicationRunner {

    private final SongRepository songRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Song> songs = songRepository.findByTitleInitialIsNull();
        // 엔티티를 고치면 @PreUpdate가 updatedAt까지 바꾸므로, 색인 컬럼만 직접 UPDATE한다
        songs.forEach(song -> songRepository.updateTitleInitial(song.getId(), TitleInitial.of(song.getTitle())));
        if (!songs.isEmpty()) {
            log.info("곡 제목 색인 글자 채움: {}곡", songs.size());
        }
    }
}
