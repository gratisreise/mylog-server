# 1. Gradle 빌드 이미지 (빌드 전용)
FROM gradle:8.2.1-jdk17 AS builder

WORKDIR /app

# Gradle 캐시 최적화 (의존성 먼저 다운로드)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 후 빌드
COPY . .
RUN gradle bootJar -x test --no-daemon

# 2. 실행용 경량 이미지
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
