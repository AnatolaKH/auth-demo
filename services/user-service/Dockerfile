# syntax=docker/dockerfile:1.7
ARG JAVA_BUILD_IMAGE=eclipse-temurin:25-jdk-alpine
ARG JAVA_RUNTIME_IMAGE=eclipse-temurin:25-jre-alpine

FROM ${JAVA_BUILD_IMAGE} as builder

WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY services/user-service/build.gradle.kts services/user-service/build.gradle.kts
COPY services/auth-service/build.gradle.kts services/auth-service/build.gradle.kts

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon :services:user-service:dependencies

COPY services/user-service/src services/user-service/src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon :services:user-service:bootJar

FROM ${JAVA_RUNTIME_IMAGE}

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /workspace/services/user-service/build/libs/*.jar /app/app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
