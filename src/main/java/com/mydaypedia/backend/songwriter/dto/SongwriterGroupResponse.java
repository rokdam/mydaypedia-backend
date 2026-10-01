package com.mydaypedia.backend.songwriter.dto;

import com.mydaypedia.backend.songwriter.Songwriter;
import java.util.Comparator;
import java.util.List;

/** 그룹 크레딧과 그 구성원. 곡 응답에 끼는 SongwriterResponse에는 구성원을 넣지 않는다 (크레딧마다 추가 로딩이 생기므로). */
public record SongwriterGroupResponse(
        Long id,
        String name,
        Long bugsArtistId,
        List<SongwriterResponse> members
) {

    public static SongwriterGroupResponse from(Songwriter group) {
        return new SongwriterGroupResponse(
                group.getId(),
                group.getName(),
                group.getBugsArtistId(),
                group.getMembers().stream()
                        .sorted(Comparator.comparing(Songwriter::getName))
                        .map(SongwriterResponse::from)
                        .toList()
        );
    }
}
