package com.mydaypedia.backend.song.dto;

import jakarta.validation.constraints.Positive;

/** 멜론 곡 번호 지정. null이면 지운다. */
public record SongMelonIdRequest(@Positive Long melonSongId) {
}
