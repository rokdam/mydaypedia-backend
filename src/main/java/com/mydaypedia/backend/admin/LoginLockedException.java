package com.mydaypedia.backend.admin;

import java.time.Duration;

/** 로그인 실패가 쌓여 잠긴 상태. 429로 응답한다. */
class LoginLockedException extends RuntimeException {

    LoginLockedException(Duration lockDuration) {
        super("로그인 실패가 너무 많습니다. " + lockDuration.toMinutes() + "분 뒤에 다시 시도하세요.");
    }
}
