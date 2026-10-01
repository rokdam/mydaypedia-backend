package com.mydaypedia.backend.concert.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SetlistReplaceRequest(
        /** 새 세트리스트 전체 (순서대로). 빈 목록이면 세트리스트를 비운다. */
        @NotNull List<@Valid SetlistEntryRequest> entries
) {
}
