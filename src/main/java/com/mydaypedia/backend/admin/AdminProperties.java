package com.mydaypedia.backend.admin;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 관리자 로그인 설정 (application.yml의 admin.*).
 *
 * @param password       관리자 비밀번호. 비어 있으면 로그인 자체가 불가능하다(= 쓰기 API 전부 막힘) — 기본값을 "막힘"으로 둬서
 *                       서버에 올릴 때 설정을 빠뜨려도 누구나 수정할 수 있게 되지 않도록.
 * @param tokenTtl       로그인 토큰 유효 시간
 * @param maxFailures    잠금 전 허용하는 로그인 실패 횟수 (IP별)
 * @param lockDuration   실패가 쌓였을 때 잠그는 시간
 */
@ConfigurationProperties(prefix = "admin")
record AdminProperties(String password, Duration tokenTtl, int maxFailures, Duration lockDuration) {
}
