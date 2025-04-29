# 1. Gradle 빌드용 이미지
FROM gradle:8.2.1-jdk17 AS builder

WORKDIR /app

# Gradle 캐시 최적화
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle build -x test || return 0

# 소스 복사 및 빌드
COPY . .
RUN gradle clean build -x test

# 2. 실제 실행용 이미지 (경량화)
FROM openjdk:17-jdk-slim

WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
