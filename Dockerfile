# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# 의존성 레이어 캐시: 빌드 스크립트만 먼저 복사
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

# 소스 복사 후 실행 가능한 jar 빌드 (테스트는 별도 CI 단계에서 수행)
COPY src src
RUN ./gradlew --no-daemon bootJar -x test

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
# logback이 /app/logs/app.log에 쓰는데, non-root 사용자는 /app 아래에 폴더를 만들 권한이 없어서
# 볼륨 마운트 없이 실행하면 기동 실패했다(실제로 겪은 문제). 미리 만들고 소유권을 넘긴다.
RUN mkdir -p /app/logs && chown spring:spring /app/logs
USER spring:spring

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
	CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
