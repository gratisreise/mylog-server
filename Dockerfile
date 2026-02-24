# Stage 1: Build - Gradle 캐시 마운트 활용
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /build

# Gradle Wrapper 및 설정 파일 복사 (레이어 캐싱)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# [핵심] GitHub Actions에서 전송된 캐시를 활용하기 위한 마운트
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

COPY src src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar -x test --no-daemon

# Stage 2: Extract - 레이어 분리
FROM eclipse-temurin:17-jdk-jammy AS extract
WORKDIR /build
COPY --from=build /build/build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract --destination extracted

# Stage 3: Final - 최소 실행 이미지
FROM eclipse-temurin:17-jre-jammy AS final
WORKDIR /app

# 보안을 위한 Non-root 유저 설정
RUN addgroup --system springgroup && adduser --system springuser --ingroup springgroup
USER springuser

# Layered Jar 구조에 맞춰 복사 (변화가 적은 순서대로)
COPY --from=extract /build/extracted/dependencies/ ./
COPY --from=extract /build/extracted/spring-boot-loader/ ./
COPY --from=extract /build/extracted/snapshot-dependencies/ ./
COPY --from=extract /build/extracted/application/ ./

EXPOSE 8080
# JarLauncher를 통한 실행 (애플리케이션 시작 최적화)
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]