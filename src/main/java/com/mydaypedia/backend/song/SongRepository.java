package com.mydaypedia.backend.song;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Song의 컬렉션 연관관계(albumLinks, artists, writerCredits)를 한 쿼리에 전부 fetch join하면
 * MultipleBagFetchException이 난다(List가 둘 이상). 그래서 크레딧만 fetch join하고, 나머지(와 검색 결과 목록 전체)는
 * application.yml의 hibernate.default_batch_fetch_size로 IN 쿼리 묶음 로딩해 N+1을 피한다.
 */
public interface SongRepository extends JpaRepository<Song, Long>, JpaSpecificationExecutor<Song> {

    @Query("SELECT DISTINCT s FROM Song s "
            + "LEFT JOIN FETCH s.writerCredits c LEFT JOIN FETCH c.songwriter WHERE s.id = :id")
    Optional<Song> findDetailById(@Param("id") Long id);

    /** 색인 글자 컬럼을 추가하기 전에 저장된 곡 (TitleInitialBackfill이 채운다) */
    List<Song> findByTitleInitialIsNull();

    /** 엔티티 수정(@PreUpdate → updatedAt 갱신)을 거치지 않고 색인 글자만 채운다 */
    @Modifying
    @Query("UPDATE Song s SET s.titleInitial = :initial WHERE s.id = :id")
    void updateTitleInitial(@Param("id") Long id, @Param("initial") String initial);
}
