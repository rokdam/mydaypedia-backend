package com.mydaypedia.backend.admin;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 관리자 인증. 단일 관리자(비밀번호 하나)라 사용자 테이블 없이, 로그인하면 무작위 토큰을 발급해
 * Redis에 만료 시간(TTL)과 함께 저장한다. 서버를 재시작해도 로그인이 유지되고, 만료되면 Redis가 알아서 지운다.
 * 무차별 대입을 막으려고 IP별 실패 횟수도 Redis에 세서, 일정 횟수를 넘으면 잠시 잠근다.
 */
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private static final String TOKEN_KEY = "admin:token:";
    private static final String FAILURE_KEY = "admin:login-failures:";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redis;
    private final AdminProperties properties;

    public record Issued(String token, Instant expiresAt) {
    }

    /** @return 비밀번호가 맞으면 새 토큰, 틀리면 empty. 잠겨 있으면 LoginLockedException. */
    Optional<Issued> login(String password, String clientIp) {
        String failureKey = FAILURE_KEY + clientIp;
        String failures = redis.opsForValue().get(failureKey);
        if (failures != null && Integer.parseInt(failures) >= properties.maxFailures()) {
            throw new LoginLockedException(properties.lockDuration());
        }
        if (!matches(password)) {
            redis.opsForValue().increment(failureKey);
            redis.expire(failureKey, properties.lockDuration());
            return Optional.empty();
        }
        redis.delete(failureKey);
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redis.opsForValue().set(TOKEN_KEY + token, "admin", properties.tokenTtl());
        return Optional.of(new Issued(token, Instant.now().plus(properties.tokenTtl())));
    }

    public boolean isValid(String token) {
        return token != null && !token.isBlank() && Boolean.TRUE.equals(redis.hasKey(TOKEN_KEY + token));
    }

    void logout(String token) {
        if (token != null) {
            redis.delete(TOKEN_KEY + token);
        }
    }

    /** 설정된 비밀번호가 없으면 항상 실패. 길이·내용 비교는 상수 시간으로(타이밍 공격 방지). */
    private boolean matches(String candidate) {
        String expected = properties.password();
        if (expected == null || expected.isBlank() || candidate == null) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), candidate.getBytes(StandardCharsets.UTF_8));
    }
}
