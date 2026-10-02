# mydaypedia backend

밴드 DAY6의 곡, 앨범, 작곡가·작사가, 공연 세트리스트를 검색하는 개인용 아카이브의 API 서버입니다.
프론트엔드: https://github.com/rokdam/mydaypedia-frontend

## 기술 스택

Java 21, Spring Boot 4, Spring Data JPA, MySQL, Redis, springdoc(Swagger), Actuator + Micrometer(Prometheus), Logback JSON 로그(Filebeat 수집용)

## 구조

도메인별 패키지(`com.mydaypedia.backend`):

| 패키지 | 내용 |
|---|---|
| `song` | 곡 검색 (JPA Specification으로 조건 조합), 곡-앨범 N:M, 곡-작곡가 크레딧 N:M |
| `album`, `artist` | 앨범, 아티스트, 앨범-아티스트 관계 |
| `songwriter` | 작곡가·작사가, 그룹 크레딧의 구성원 |
| `concert` | 투어, 공연 회차, 세트리스트 |
| `playlist` | 플레이리스트 |
| `admin` | 관리자 로그인 (Redis 토큰), 쓰기 API 보호 |
| `common` | 공통 예외 처리 |

곡 검색 예: `GET /api/songs?composerIds=16,13&composerMatch=EXACT&month=1`
작곡가 조합 방식은 `ANY`(한 명이라도), `ALL`(모두 같이), `EXACT`(고른 멤버만, 외부 작곡가는 허용) 세 가지입니다.

## 실행

MySQL과 Redis가 필요합니다.

```bash
docker run -d --name mydaypedia-mysql -p 3306:3306 \
  -e MYSQL_DATABASE=mydaypedia -e MYSQL_USER=mydaypedia \
  -e MYSQL_PASSWORD=mydaypedia -e MYSQL_ROOT_PASSWORD=root mysql:8.4
docker run -d --name mydaypedia-redis -p 6379:6379 redis:7-alpine

DB_PASSWORD=mydaypedia SWAGGER_ENABLED=true ./gradlew bootRun
```

- `DB_PASSWORD`는 필수입니다. 기본값이 없어서 빠뜨리면 기동하지 않습니다.
- `SWAGGER_ENABLED=true`일 때만 Swagger UI(`/swagger-ui.html`)가 켜집니다.
- `ADMIN_PASSWORD`를 주지 않으면 관리자 로그인이 막혀 쓰기 API를 쓸 수 없습니다.
- 데이터는 비어 있는 상태로 시작합니다. 개인 학습용으로 수집한 데이터와 수집 스크립트는 저장소에 포함하지 않았습니다.

```bash
./gradlew test   # H2 인메모리 DB로 실행
./gradlew build
```

## 개발 방식

Claude Code와 함께 개발했습니다. 설계 결정, 스키마 변경 이력, 실제로 겪은 버그는 `CLAUDE.md`에 기록되어 있습니다.
