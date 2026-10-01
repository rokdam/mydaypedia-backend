# backend (Spring)

Spring Boot 기반 백엔드. 전체 스택/디렉토리 구조는 [루트 CLAUDE.md](../CLAUDE.md) 참고.

DAY6 개인 팬질 기록 + 아카이브 서비스의 API 서버. 곡/공연/TV 활동 아카이브를 제공하고, 취향대로 곡을 찾아 플레이리스트를 만들 수 있다. 인증 없는 개인용(single-user) 서비스로 시작했다.

## 스택 상세

- Spring Boot 4.1.1 (Spring Framework 7)
- Java 21 (Gradle toolchain으로 고정)
- 빌드 도구: Gradle (Groovy DSL), 래퍼(`./gradlew`) 포함
- 패키지 루트: `com.mydaypedia.backend`
- 진입점: `src/main/java/com/mydaypedia/backend/BackendApplication.java`

## 의존성

- Web: `spring-boot-starter-web`, `spring-boot-starter-validation`
- RDB: `spring-boot-starter-data-jpa` + `com.mysql:mysql-connector-j`
- NoSQL: `spring-boot-starter-data-redis`
- 모니터링: `spring-boot-starter-actuator` + `micrometer-registry-prometheus`
- API 문서화: `springdoc-openapi-starter-webmvc-ui:3.1.1`
- 로그: `net.logstash.logback:logstash-logback-encoder:9.0` (JSON 파일 로그, Filebeat가 수집)
- 개발 편의: Lombok, `spring-boot-devtools`
- 테스트: `spring-boot-starter-test`, H2(테스트 전용 인메모리 DB, `src/test/resources/application.yml`)

## 명령어

- 빌드: `./gradlew build`
- 테스트: `./gradlew test`
- 로컬 실행: `DB_PASSWORD=mydaypedia ./gradlew bootRun` (`DB_PASSWORD`는 필수 — 아래 설정 참고)
- Docker 이미지 빌드: `docker build -t mydaypedia-backend:local .` (멀티스테이지: `eclipse-temurin:21-jdk`로 빌드 → `eclipse-temurin:21-jre-alpine`로 실행, non-root 유저, `/actuator/health` 기반 HEALTHCHECK 포함). 런타임 단계에서 `/app/logs`를 미리 만들고 spring 유저에게 소유권을 준다 — 없으면 logback이 로그 폴더를 못 만들어 **볼륨 마운트 없이 단독 실행 시 기동 실패**했다(compose는 logs 볼륨을 붙여서 가려져 있었음, 실제로 겪은 문제)
- Docker로 단독 실행(기존 docker-compose 네트워크에 수동으로 붙이기): `docker run -e DB_HOST=mydaypedia-mysql -e REDIS_HOST=mydaypedia-redis --network docker_default -p 8081:8080 mydaypedia-backend:local`
- **권장**: `docker-compose.yml`에 `backend` 서비스로 이미 등록되어 있다 (`profiles: app`). `cd ../infra/docker && docker compose --profile app up -d --build`로 mysql/redis 대기 후 자동으로 뜬다. 자세한 내용: [infra/docker/README.md](../infra/docker/README.md)

## 설정 (`src/main/resources/application.yml`)

- MySQL 접속 정보: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` 환경변수로 오버라이드(기본값은 로컬 개발용). **`DB_PASSWORD`는 기본값 없음 — 없으면 기동 실패**(GitHub 공개 전 보안 검토, 2026-10-01: 공개 저장소에 적힌 로컬 비밀번호로 운영 DB에 붙는 일이 없게). 로컬 docker compose MySQL은 `mydaypedia`, compose의 backend 서비스는 알아서 넣어 준다.
- Redis 접속 정보: `REDIS_HOST`, `REDIS_PORT`
- Actuator: `/actuator/health`, `/actuator/prometheus` (Prometheus 스크레이핑 대상)
- Swagger UI: `/swagger-ui.html`, OpenAPI 스펙: `/v3/api-docs` — **기본은 꺼짐**(404). 환경변수 `SWAGGER_ENABLED=true`일 때만 켜진다(로컬 `SWAGGER_ENABLED=true ./gradlew bootRun`, docker compose의 backend 서비스는 켜 둠). 외부 호스팅에 올릴 때 API 구조가 노출되지 않게 하려는 것(사용자 요청) — 운영 프로필에서 끄는 대신 기본값을 끔으로 둬서, 호스팅에 설정을 빠뜨려도 노출되지 않는다
- `jpa.hibernate.ddl-auto: update` — 마이그레이션 도구(Flyway 등) 도입 전까지 임시. 스키마가 안정되면 `validate` + Flyway/Liquibase로 전환할 것

## API 도메인

- `song/` — 곡 아카이브. `Song` 엔티티, CRUD 전체. `GET /api/songs`는 아래 조건을 전부 선택(AND 조합)으로 지원한다 (Specification 기반, `SongSearchCriteria`로 묶어서 서비스에 전달):
  - `title` — 대소문자 무시 부분일치
  - `initial` — 제목 색인 글자(가나다 색인): `ㄱ`~`ㅎ`(쌍자음은 기본 자음에 포함 — ㄲ→ㄱ, ㄸ→ㄷ, ㅃ→ㅂ, ㅆ→ㅅ, ㅉ→ㅈ), `A`~`Z`, `#`(숫자·외국 문자). `Song.titleInitial`(`songs.title_initial`)과 정확 일치. 값은 `song.TitleInitial.of(title)`이 계산(앞의 괄호·따옴표 등 기호는 건너뜀, 단위 테스트 `TitleInitialTest`)하고 `@PrePersist`/`@PreUpdate`에서 매번 다시 채운다. 컬럼 추가 전 곡은 `TitleInitialBackfill`(ApplicationRunner)이 기동 시 채움 — 엔티티 수정 대신 벌크 UPDATE(`updateTitleInitial`)로 해서 `updatedAt`을 건드리지 않는다(2026-09-28에 238곡 채움)
  - `trackNumber` — 수록 앨범 중 하나라도 그 트랙 번호면 포함(예: 1 → 각 앨범의 1번 트랙, 73곡)
  - `album` — 수록 앨범 중 하나라도 **제목**(`albums.title`)이 대소문자 무시 부분일치하면 포함 (앨범 없는 곡은 제외됨), `albumId` — 수록 앨범 중 하나가 `/api/albums`의 그 id면 포함
  - `artistId` — 부른 아티스트(`/api/artists`의 id)로 정확 일치 (피처링으로만 참여한 곡 포함)
  - `albumType` — 수록 앨범 중 하나라도 그 유형(REGULAR/EP/SINGLE/OST/COMPILATION)이면 포함. 싱글로 먼저 나오고 정규에 재수록된 곡은 SINGLE·REGULAR 양쪽에 걸린다(2026-09-28 기준 21곡). COMPILATION은 추적 아티스트가 부른 곡이 없어 0건
  - `genre` — **장르 하나** 단위(대소문자 무시). 한 곡에 장르가 여럿이면 `"댄스/팝, 인디, 락/메탈"`처럼 `", "`로 이어 저장하고, 검색은 앞뒤에 구분자를 붙여 `", 인디,"` 같은 통째 항목으로 찾는다(그냥 부분일치하면 "팝"이 "댄스/팝"에도 걸림). 2026-09-28에 기존 데이터의 `",\n"` 구분(벅스 표기 그대로 줄바꿈이 섞여 있었음, 53곡)을 SQL `REGEXP_REPLACE`로 `", "`로 정리했고
  - `composerIds`, `lyricistIds`(여러 명, `composerIds=16&composerIds=13` 또는 `composerIds=16,13`) + `composerMatch`, `lyricistMatch`(`CreditMatch`: `ANY` 기본 — 한 명이라도 참여한 곡 / `ALL` — 전부 같이 참여한 곡 / `EXACT` — 전부 같이 참여 + 같은 그룹의 다른 멤버는 같은 역할로 불참, 외부 작곡가는 허용. 예: 원필+성진 EXACT → '홍지상, 성진, 원필' 포함, '홍지상, 성진, Young K, 원필' 제외. 한 명만 골라도 쓸 수 있다(원필 단독 작곡). "다른 멤버"는 고른 사람이 속한 그룹 크레딧의 구성원(`Songwriter.members`)으로 판단하고, 그룹 크레딧('DAY6 (데이식스)') 자체가 붙은 곡은 전원 참여로 보아 EXACT에서 제외한다 — `SongSpecifications.noOtherMembers`의 NOT EXISTS 서브쿼리). **텍스트 검색이 아니라 `songwriter/`의 id로 검색한다.** 프론트에서 `/api/songwriters` 목록을 보여주고 클릭해서 고르는 방식을 전제로 한 설계. ANY는 크레딧 join 하나에 IN, ALL은 사람마다 크레딧 join을 따로 걸어 AND로 묶는다(크레딧 행 하나가 여러 사람을 동시에 만족할 수 없으므로). 작곡 조건과 작사 조건끼리는 AND. 예전 단일 파라미터 `composerId`/`lyricistId`는 없앴다. 본인 크레딧뿐 아니라 **그 사람이 구성원인 그룹 크레딧**(예: 'DAY6 (데이식스)' 작곡)도 포함한다 (`SongSpecifications.writtenBy`의 서브쿼리, 아래 songwriter 그룹 설명 참고)
  - `songType` — `ORIGINAL`/`OST`/`CM_SONG` 정확 일치 (기본값 `ORIGINAL`)
  - `hasFeaturing` — true/false. `featuredArtist` 컬럼의 null 여부로 판단
  - `year`, `month`, `day` — `releaseDate`에서 `YEAR()`/`MONTH()`/`DAY()` SQL 함수로 추출해 비교. 서로 독립적으로 조합 가능(예: year 없이 month만 주면 "연도 무관 6월생 곡" 전체 조회)
- **멜론 곡 번호** `Song.melonSongId`(`songs.melon_song_id`, nullable): 프론트의 "담은 곡 → 멜론 원클릭 재생 링크"용. 곡 등록/수정(PUT) 요청에는 넣지 않고 전용 `PUT /api/songs/{id}/melon-id`(`{melonSongId}`, null이면 지움, 관리자 토큰 필요)로만 바꾼다 — 전체 교체 PUT으로 곡을 고칠 때 값이 지워지지 않게. 응답 `SongResponse.melonSongId`
- `album/` — 앨범. `Album`(title, releaseDate, `albumType`(`AlbumType`: REGULAR/EP/SINGLE/OST/COMPILATION — 벅스 분류 기준), `artistName`(대표 아티스트 — DAY6가 참여만 한 다른 가수의 싱글/트리뷰트/컴필레이션도 같이 관리하므로 필요), `bugsAlbumId`(벅스 앨범 id, unique — 출처 추적 + 재등록 시 중복 방지, 겹치면 409)) + `GET /api/albums`(발매일→제목순, 클릭 선택용), `POST`(등록), `DELETE`(수록곡이 걸려있으면 409). 곡과 앨범은 N:M이다 — `song.SongAlbum`(`song_albums`: song_id, album_id, `trackNumber`, `bugsTrackId` unique, `(song_id, album_id)` unique). 같은 곡이 여러 앨범에 실리기 때문(Every DAY6 월간 싱글 → 정규 SUNRISE/MOONRISE 재수록, OST Part → OST 전곡 앨범 등). 벅스 트랙 id는 앨범마다 달라서 곡이 아니라 수록 정보에 붙는다. 곡 등록/수정 요청은 `albums: [{albumId, trackNumber, bugsTrackId}]`(수정 시 diff 방식으로 맞춤 — 크레딧과 같은 이유), 기존 곡에 수록 앨범 하나 추가는 `POST /api/songs/{id}/albums`(중복 409), 응답은 `albums: [{album: AlbumSummaryResponse, trackNumber, bugsTrackId}]`(앨범 발매일 오름차순). 곡 응답에는 색인 글자 `titleInitial`도 있다. `Song.releaseDate`는 곡의 최초 발매일(가장 먼저 나온 수록 앨범 기준)이다.
  - 스키마 이력(`ddl-auto: update`는 컬럼을 지워주지 않아서 로컬 MySQL에서 수동 처리, 둘 다 당시 songs 0건): ① `songs.album_name` → `DROP COLUMN` ② `songs.album_id`(N:1 시절 FK) → `DROP FOREIGN KEY` + `DROP COLUMN`. ③ `songs.composer`/`songs.lyricist`(작곡가/작사가를 `song_writer_credits` N:M으로 옮기기 전의 문자열 컬럼, 238곡 모두 비어 있었음) → 2026-09-28 `DROP COLUMN`(사용자 요청). 다른 환경에 기존 DB가 있으면 같은 처리가 필요하다.
  - **아티스트 연결**: `AlbumArtist`(album 패키지, `album_artists` 테이블, `(album_id, artist_id)` 유니크)가 `Album`—`artist.Artist`를 관계(`AlbumArtistRole`: RELEASE/JOIN/COMPILATION — 벅스 탭 `발매`/`참여`/`컴필레이션` 기준)와 함께 잇는 N:M 조인 엔티티다. 한 앨범이 아티스트마다 다른 관계를 가질 수 있다(예: Every DAY6 = DAY6 RELEASE + Young K JOIN). `GET /api/albums?artistId=&role=`로 필터(예: Young K + RELEASE = 솔로 발매작), `POST /api/albums/{id}/artists`(`{artistId, role}`, 중복 409), `DELETE /api/albums/{id}/artists/{artistId}`. 앨범 삭제 시 연결은 cascade로 같이 지워진다.
  - `AlbumResponse`에는 `artists: [{artistId, name, role}]`가 들어간다. 목록은 `findAllWithArtists`(fetch join)로 N+1 없이 가져오고, artistId/role 필터는 fetch join 대상에 직접 걸면 응답의 artists 목록까지 잘리므로 `EXISTS` 서브쿼리로 건다. 곡 응답(`SongResponse.album`)은 아티스트 연결을 뺀 `AlbumSummaryResponse`를 쓴다 — 곡 검색 결과마다 `artistLinks`를 지연 로딩하는 N+1을 피하려고
  - `artistName`은 벅스에 표시되는 대표 아티스트 문자열 그대로다(추적 대상이 아닌 다른 가수 포함, 여러 가수 참여 앨범은 `Various Artists`). 추적 아티스트와의 관계는 `artistName`이 아니라 `AlbumArtist`로 판단한다
  - 로컬 mysql 클라이언트로 한글이 `???`로 보이면 `--default-character-set=utf8mb4`를 붙일 것 (저장은 정상 utf8mb4)
- 곡을 부른 아티스트: `Song.artists`(`@ManyToMany`, `song_artists` 조인 테이블) — 추적 아티스트(`artist.Artist`) 중 그 곡을 부른 사람. 멤버가 피처링으로만 참여한 곡(예: Ben&Ben 「Leaves (feat. Young K)」)도 여기에 멤버가 들어간다. 추적 대상이 아닌 피처링 가수는 기존처럼 `featuredArtist` 문자열. 곡 등록/수정 요청은 `artistIds`, 응답은 `artists: ArtistResponse[]`
  - `Song`의 컬렉션(albumLinks, artists, writerCredits)을 한 쿼리에 전부 fetch join하면 `MultipleBagFetchException`이 나서, `findDetailById`는 크레딧만 fetch join하고 나머지는 `application.yml`의 `hibernate.default_batch_fetch_size: 100`(IN 쿼리 묶음 로딩)으로 N+1을 막는다. 검색 결과 목록도 같은 방식
- `artist/` — 추적하는 아티스트(그룹 DAY6, 멤버 솔로 Young K 등). `Artist`(name, `bugsArtistId` unique) + `GET /api/artists`(이름순), `POST`(등록, bugsArtistId 중복 409), `DELETE`(앨범에 연결되어 있으면 409). 앨범과의 관계는 `album.AlbumArtist`가 관리한다(위 album 설명 참고)
- `songwriter/` — 작곡가/작사가. `Songwriter`(name + `bugsArtistId` unique nullable — 벅스 크레딧에 링크가 있는 사람만 채워짐, 링크 없이 이름만 있는 크레딧은 null) + `GET /api/songwriters`(전체 목록, 이름순 — 클릭 선택용), `POST`(등록), `DELETE`(곡에 크레딧으로 걸려있으면 409). 작곡/작사 역할 자체는 `song.SongWriterCredit`(song 패키지 소속, package-private)이 `Song`—`Songwriter`를 역할(`SongWriterRole`: COMPOSER/LYRICIST)과 함께 잇는 N:M 조인 엔티티로 관리한다. 한 곡에 작곡/작사 각각 여러 명이 붙을 수 있고, 한 사람이 작곡+작사를 동시에 맡을 수도 있다.
  - **그룹 크레딧**: 벅스 크레딧에 개인이 아니라 그룹 이름이 올라간 경우가 있다(MOONRISE CD 전용 「The Day」 수록곡 Final Ver. 5곡의 'DAY6 (데이식스)' 작곡/작사). 크레딧을 멤버별로 쪼개면 "그룹 크레딧"이라는 원본 정보가 사라져서, 대신 `Songwriter.members`(`songwriter_members`: group_id, member_id 자기참조 N:M)로 그룹 구성원을 지정한다. `GET/PUT /api/songwriters/{id}/members`(`{memberIds}`, 통째로 교체, 자기 자신은 400). 곡 응답의 크레딧 표시는 그대로 'DAY6 (데이식스)'이고, 검색만 멤버로도 걸린다. 2026-09-28 기준 'DAY6 (데이식스)'(id 44) 구성원 = 성진, 임준혁, 원필, 도운, Young K, Jae (사용자가 지정한 6명, 시기별 멤버 구성은 고려하지 않음). 곡 응답에 끼는 `SongwriterResponse`에는 구성원을 넣지 않는다(크레딧마다 추가 로딩)
  - `IllegalArgumentException`은 `GlobalExceptionHandler`가 400으로 변환한다 (서비스 계층의 의미상 잘못된 요청용)
  - `Song.replaceWriterCredits(composers, lyricists)`로 곡 생성/수정 시 크레딧을 갱신한다. **주의**: 무조건 기존 크레딧을 `clear()`하고 전부 다시 `add()`하면 안 된다 — `(song_id, songwriter_id, role)` 유니크 제약이 있는 상태에서, Hibernate가 같은 flush 안에서 INSERT를 DELETE보다 먼저 실행하기 때문에 안 바뀐 크레딧(예: 작곡가 2명 중 1명만 교체)까지 중복 키 오류(409)가 난다 (실제로 겪은 버그). 그래서 `replaceCreditsForRole`이 필요한 것만 지우고 필요한 것만 추가하는 diff 방식으로 구현되어 있다.
- `playlist/` — 플레이리스트. `Playlist` — `PlaylistSong`(곡 순서 포함 조인 엔티티) 1:N. CRUD + `POST/DELETE /api/playlists/{id}/songs`로 곡 추가/제거
- `admin/` — 관리자 인증과 쓰기 API 보호. 단일 관리자(비밀번호 하나, `admin.password` = 환경변수 `ADMIN_PASSWORD`, **비어 있으면 로그인 불가 = 쓰기 API 전부 막힘**이 기본값 — 호스팅 때 설정을 빠뜨려도 누구나 수정할 수 있게 되지 않도록. 로컬 docker compose만 기본 `admin`)
  - `POST /api/admin/login`(`{password}` → `{token, expiresAt}`, 틀리면 401), `POST /api/admin/logout`(토큰 즉시 무효화), `GET /api/admin/me`(유효하면 204, 아니면 401)
  - 토큰은 무작위 32바이트(base64url)를 **Redis**에 `admin:token:<토큰>` 키로 TTL(`admin.token-ttl`, 12h)과 함께 저장 — 재시작해도 로그인 유지, 만료는 Redis가 처리. 사용자 테이블/JWT 없음. 비밀번호 비교는 `MessageDigest.isEqual`(상수 시간)
  - 로그인 실패는 IP별로 `admin:login-failures:<ip>`에 세서 `admin.max-failures`(5)회 넘으면 `admin.lock-duration`(15m) 동안 429(맞는 비밀번호도 거부). IP는 nginx가 덮어쓴 `X-Real-IP`(없으면 remoteAddr) — 클라이언트가 위조할 수 있는 `X-Forwarded-For`는 안 씀. 백엔드 포트를 외부에 직접 열면 X-Real-IP도 위조 가능하므로 운영에선 nginx 뒤에만 둘 것
  - `AdminAuthFilter`(`OncePerRequestFilter`): `/api/**` 중 GET/HEAD/OPTIONS가 아닌 요청은 `Authorization: Bearer <토큰>`이 유효해야 통과, 아니면 401 JSON(`ErrorResponse`). 로그인만 예외. Spring Security를 안 쓴 이유: 단일 관리자 + 토큰 한 종류라 필터 하나로 충분하고, 쿠키를 안 써서 CSRF 처리도 필요 없다
  - **주의(실제로 겪은 것)**: Spring Boot 4는 Jackson 3(`tools.jackson`)을 쓴다 — `com.fasterxml.jackson.databind.ObjectMapper` 빈이 없어서 주입하면 기동 실패. 필터는 `tools.jackson.databind.json.JsonMapper`를 주입한다
- `common/exception/` — `GlobalExceptionHandler`(`@RestControllerAdvice`)가 `ResourceNotFoundException`→404, `DuplicateResourceException`→409, `DataIntegrityViolationException`(FK 참조 중 삭제, 유니크 제약 충돌 등)→409, `MethodArgumentNotValidException`→400(필드별 메시지)으로 변환
- `concert/` — 공연(콘서트/팬미팅/페스티벌)과 세트리스트. 투어 → 회차 2단계.
  - `Tour`(`tours`: name, `eventType`(`EventType`: CONCERT/FANMEETING/FESTIVAL), memo) — 투어 기간(startDate~endDate)은 **저장하지 않고 회차 날짜의 min/max로 계산**(회차 추가/삭제해도 어긋나지 않게). `GET /api/tours`(최근 투어부터, 회차 수 포함), `GET /api/tours/{id}`(회차 목록 포함), `POST`, `DELETE`(회차가 남아 있으면 400)
  - `Show`(`shows`: 공연 회차 = 날짜·장소 단위): tour(nullable — 페스티벌 출연/단발 공연은 투어 없음), eventType(생략 시 투어 유형, 투어도 없으면 400), title(페스티벌명 등), showDate, startTime, venue, city, country, memo, `sourceUrl`(unique — 출처 추적 + 재등록 시 중복 방지), 공연 아티스트 `artists`(`show_artists` N:M → `artist.Artist`, 멤버 솔로/유닛 공연도 같은 구조). `GET /api/shows?tourId=&eventType=&artistId=&year=&songId=`(날짜순, songId = 세트리스트에서 그 곡을 부른 회차), `GET /api/shows/{id}`(세트리스트 포함), `POST`(세트리스트 함께 등록 가능), `PUT /api/shows/{id}/setlist`(통째로 교체), `DELETE`(세트리스트 cascade)
  - `SetlistEntry`(`setlist_entries`, `(show_id, position)` unique): position(1부터, 요청 목록 순서), `entryType`(`SetlistEntryType`: SONG / OTHER — API는 OTHER도 받지만 데이터는 SONG만 넣는다. 곡이 아닌 순서(VCR·토크)는 저장하지 않기로 함), song(nullable — songs에 있으면 연결), title(출처 표기 그대로, 곡이 없으면 필수 — 커버곡·미발매곡·VCR), encore, memo(예: 'Acoustic ver.', 'Cover of ...'). 응답 title은 표기가 있으면 표기, 없으면 곡 제목. songId·title 둘 다 없으면 400
  - 세트리스트 교체는 **비우기 → `flush()` → 채우기** 순서다. (show_id, position) 유니크 제약 때문에 flush 없이 하면 Hibernate가 새 INSERT를 옛 DELETE보다 먼저 실행해 같은 position에서 중복 키 오류가 난다(Song 크레딧에서 겪은 것과 같은 문제 — 여기선 전체 교체라 diff 대신 flush로 해결)
  - 투어 회차별 세트리스트는 회차마다 따로 저장한다(투어 공통 세트리스트 + 회차별 차이 같은 구조는 두지 않음)
  - `EventType`에 `AWARD`(시상식·연말 가요제), `UNIVERSITY_FESTIVAL`(대학 축제), `SHOWCASE`(쇼케이스·청음회) 추가 — FESTIVAL은 페스티벌 + 참여 공연·행사(KCON, 라이브 클럽 데이, 다른 가수 공연 게스트 등). 공연 쪽 enum 컬럼은 `@JdbcTypeCode(SqlTypes.VARCHAR)`로 VARCHAR 저장(MySQL ENUM 컬럼이 되면 `ddl-auto: update`가 값 추가를 반영하지 않으므로)
  - 나무위키 외 출처로 수동 추가한 세트리스트 (세트리스트 교체 API로 넣음):
    - 월드 투어 'Youth' 서울 2018-06-22·23·24(올림픽홀): 네이버 블로그 https://m.blog.naver.com/gaeeeeeul/221305419151 ('첫/중/막 콘 셋 리스트'). 26곡 + 막콘(06-24)만 앵콜 My Day. 블로그 표기 정리: '(new!)' 제거, '- 오케스트라 ver.'는 메모로, Congratulation→Congratulations, 아 왜→아 왜 (I Wait), Shoot Me!→Shoot Me, Warning→WARNING!. 이후 사용자 요청으로 나머지 25개 회차(지방·해외·2019 앵콜 서울 공연 포함)에도 공통 26곡을 복사했다(막콘 앵콜 My Day는 서울 06-24에서만 확인돼 제외). 즉 Youth 투어 28회 전부 세트리스트 있음 — 서울 첫 주 외 회차는 실제 차이가 반영되지 않은 추정치
    - 월드 투어 'GRAVITY' 31회 전부: 사용자가 준 네이버 블로그 이미지 2장(블로그 adyor, 2019-08-18 게시, IMG_3492/IMG_3493)을 옮겨 적은 28곡. 앵콜 구분 없음. 멤버 솔로 무대('How to love (성진 솔로)' 등)는 곡 연결 + 메모, 매시업('태양처럼 x shape of you' 등)은 표기 그대로 제목 + 앞쪽 DAY6 곡에 연결 + 메모 '매시업: ...', 나머지는 제목을 곡 DB 표기로 맞춤. 투어 전 회차에 같은 세트리스트를 복사한 추정치(도시별 차이 미반영)
  - 공연 데이터 출처 라이선스: 나무위키 CC BY-NC-SA 2.0 KR — 개인용 비상업 서비스 전제, 회차마다 sourceUrl로 출처 보존
- 아직 없음: TvActivity(TV 활동)/FanLog(개인 기록) — 다음 단계 예정
- **중요**: `open-in-view: false`이므로 지연 로딩 연관관계(`Song.albumLinks`, `Song.artists`, `Song.writerCredits`, `Album.artistLinks`, `Playlist.items`, `PlaylistSong.song`)는 반드시 서비스 계층(트랜잭션 안)에서 DTO로 변환해야 한다. 컨트롤러에서 변환하면 `LazyInitializationException`이 난다 (실제로 겪은 버그 — `SongService`/`PlaylistService`가 엔티티가 아니라 DTO를 직접 반환하는 이유). 단건 조회는 `findDetailById`(JOIN FETCH)로 N+1 없이 가져온다. 검색(`search`)은 결과 목록이라 fetch join을 안 붙였고, 곡마다 크레딧을 lazy loading하는 N+1이 있다 — 트랜잭션 안에서 일어나니 에러는 안 나지만, 검색 결과가 많아지면 최적화 여지가 있다.

## 데이터 적재 도구

- 초기 데이터를 넣은 수집·정리 스크립트(`scripts/`)와 그 설명은 **git으로 관리하지 않는다**(사용자 결정, 2026-10-01 — 공개 저장소에 올리지 않음). 로컬에만 있고, 설명은 로컬 전용 문서 `scripts/CLAUDE.md`에 있다

## 로깅 (`src/main/resources/logback-spring.xml`)

- 콘솔 출력 + `logs/app.log`(JSON, `logstash-logback-encoder`)에 동시 기록
- `logs/`는 git-ignored. Filebeat(`infra/elasticsearch/filebeat.yml`)가 이 파일을 tail해서 Elasticsearch로 전송
- 커스텀 필드는 `app_name`으로 추가한다 (`service`는 ECS/Filebeat 기본 템플릿에서 객체 타입으로 예약되어 있어 문자열로 쓰면 색인이 거부됨 — 실제로 겪은 문제)

## 로컬 실행 전제조건

- `bootRun`으로 실제 기동하려면 MySQL(3306)과 Redis(6379)가 떠 있어야 한다: `docker compose -f ../infra/docker/docker-compose.yml up -d`
- 접속 정보는 `application.yml` 기본값과 `infra/docker/docker-compose.yml`이 서로 맞춰져 있다 (DB `mydaypedia`/`mydaypedia`/`mydaypedia`).
- `./gradlew test`는 테스트 전용 H2 인메모리 DB를 사용하므로 MySQL/Redis 없이도 통과한다.
- 기동 확인됨: `/actuator/health` → UP, `/actuator/prometheus` → 200, `/swagger-ui/index.html` → 200(SWAGGER_ENABLED=true일 때, 없으면 404)
- Song/Playlist API는 실제 MySQL에 붙여서 CRUD, 검색, 플레이리스트 곡 추가/제거/중복(409)/FK 충돌(409)/404/400 검증 케이스까지 curl로 확인함
- Song 검색의 year/month/day/songType/hasFeaturing 조건과 조합 검색(예: genre+year)도 실제 MySQL로 확인함
- Songwriter: 작곡가 여러 명 등록, 곡에 작곡 2명+작사 1명 연결, composerId/lyricistId 검색, 크레딧 일부 교체(작곡가 2명 중 1명만 교체) 후 안 바뀐 쪽 유지 확인, 참조 중인 songwriter 삭제 시 409, 참조 풀린 뒤 삭제 성공까지 실제 MySQL로 확인함

## 패키지 구조 규칙

- **도메인 기준 패키징**을 쓴다 (`song/`, `playlist/`, 앞으로 `performance/`, `tvActivity/`, `fanLog/` 등). `controller/`, `service/`, `repository/`로 나누는 레이어 기준 패키징은 쓰지 않는다 — 도메인이 계속 늘어날 예정이라, 도메인별로 묶어야 한 기능 관련 코드를 한곳에서 보기 쉽고 나중에 분리하기도 쉽다. 여러 도메인이 공유하는 진짜 횡단 관심사(예외 처리 등)만 `common/`에 둔다.
- **패키지 경계는 Java 접근 제한자로 강제한다.** 다른 패키지에서 참조하지 않는 클래스(대부분의 `*Service`, `*Controller`, `*Repository`)는 `public`을 붙이지 않는다 — 실수로 다른 도메인이 이 도메인의 내부 구현(Service/Repository)에 직접 의존하는 걸 컴파일 단계에서 막아준다. 다른 패키지(`xxx.dto`, 다른 도메인)에서 실제로 참조하는 것만 `public`으로 남긴다: 엔티티(`Song`, `Album`, `AlbumArtist`, `Artist`, `Tour`, `Show`, `SetlistEntry`, `Playlist`, `PlaylistSong` — DTO 변환 시 필요), DTO 레코드(요청/응답 계약), 그리고 다른 도메인이 실제로 의존하는 Repository(`SongRepository`는 `PlaylistService`·`ShowService`가 곡 존재 확인에, `ArtistRepository`는 `ShowService`도 씀, `AlbumRepository`는 `SongService`가 albumId 확인에, `ArtistRepository`는 `AlbumService`가 artistId 확인에 씀). Spring Data JPA 리포지토리(JDK 동적 프록시)와 `@Service`/`@RestController` 빈 둘 다 package-private이어도 정상 동작함을 확인했다.
- 새 도메인을 추가할 때도 이 두 규칙을 따른다.
