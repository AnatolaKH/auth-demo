# syntax=docker/dockerfile:1.7
ARG JAVA_BUILD_IMAGE=eclipse-temurin:25-jdk-alpine
ARG JAVA_RUNTIME_IMAGE=eclipse-temurin:25-jre-alpine

FROM ${JAVA_BUILD_IMAGE} as builder

WORKDIR /app

COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon dependencies

COPY src ./src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon bootJar

FROM ${JAVA_RUNTIME_IMAGE}

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
