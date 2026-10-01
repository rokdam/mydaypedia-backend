package com.mydaypedia.backend.admin;

import com.mydaypedia.backend.admin.dto.LoginRequest;
import com.mydaypedia.backend.admin.dto.LoginResponse;
import com.mydaypedia.backend.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자 로그인. 발급받은 토큰을 Authorization: Bearer 헤더로 보내야 등록/수정/삭제 API를 쓸 수 있다.")
class AdminAuthController {

    private final AdminAuthService authService;

    @PostMapping("/login")
    @Operation(summary = "관리자 로그인", description = "비밀번호가 틀리면 401, 실패가 쌓여 잠기면 429.")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return authService.login(request.password(), clientIp(http))
                .<ResponseEntity<?>>map(issued -> ResponseEntity.ok(new LoginResponse(issued.token(), issued.expiresAt())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), "비밀번호가 올바르지 않습니다.")));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "토큰을 즉시 무효화한다.")
    public ResponseEntity<Void> logout(HttpServletRequest http) {
        authService.logout(AdminAuthFilter.bearerToken(http));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "로그인 상태 확인", description = "토큰이 유효하면 204, 아니면 401. 어드민 화면 진입 시 확인용.")
    public ResponseEntity<?> me(HttpServletRequest http) {
        return authService.isValid(AdminAuthFilter.bearerToken(http))
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), "관리자 로그인이 필요합니다."));
    }

    @ExceptionHandler(LoginLockedException.class)
    ResponseEntity<ErrorResponse> handleLocked(LoginLockedException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS.value(), ex.getMessage()));
    }

    /**
     * 로그인 실패 잠금 기준 IP. 프론트 nginx를 거쳐 오면 nginx가 덮어쓴 X-Real-IP를 쓴다.
     * (X-Forwarded-For는 클라이언트가 보낸 값이 앞에 남아 위조할 수 있어서 쓰지 않는다.
     * 백엔드 포트를 외부에 직접 열면 X-Real-IP도 위조 가능하므로, 운영에서는 백엔드를 nginx 뒤에만 둘 것)
     */
    private static String clientIp(HttpServletRequest http) {
        String realIp = http.getHeader("X-Real-IP");
        return realIp != null && !realIp.isBlank() ? realIp.trim() : http.getRemoteAddr();
    }
}
