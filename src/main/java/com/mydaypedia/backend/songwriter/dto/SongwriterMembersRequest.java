package com.mydaypedia.backend.songwriter.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SongwriterMembersRequest(
        /** 그룹 크레딧의 구성원 — 기존 /api/songwriters id 목록. 빈 목록이면 구성원 해제. */
        @NotNull List<Long> memberIds
) {
}
