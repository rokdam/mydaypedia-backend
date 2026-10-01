package com.mydaypedia.backend.admin;

import com.mydaypedia.backend.common.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

/**
 * 쓰기 API 보호. /api/** 중 조회(GET/HEAD/OPTIONS)가 아닌 요청은 유효한 관리자 토큰(Authorization: Bearer ...)이 있어야 한다.
 * 로그인 요청만 예외. 공개 화면(곡 검색 등)은 GET만 쓰므로 영향이 없다.
 * Spring Security 대신 필터 하나로 둔 이유: 단일 관리자 + 토큰 한 종류라 이것으로 충분하고, 쿠키를 안 쓰니 CSRF 처리도 필요 없다.
 */
@Component
@RequiredArgsConstructor
class AdminAuthFilter extends OncePerRequestFilter {

    private static final Set<String> READ_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    static final String LOGIN_PATH = "/api/admin/login";

    private final AdminAuthService authService;
    /** Spring Boot 4는 Jackson 3(tools.jackson)을 쓴다 — com.fasterxml ObjectMapper 빈은 없다(실제로 겪은 문제). */
    private final JsonMapper jsonMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/") || READ_METHODS.contains(request.getMethod()) || path.equals(LOGIN_PATH);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (authService.isValid(bearerToken(request))) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        jsonMapper.writeValue(response.getWriter(),
                ErrorResponse.of(HttpServletResponse.SC_UNAUTHORIZED, "관리자 로그인이 필요합니다."));
    }

    static String bearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        return header != null && header.startsWith("Bearer ") ? header.substring(7).trim() : null;
    }
}
