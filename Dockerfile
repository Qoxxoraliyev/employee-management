FROM eclipse-temurin:17-jdk-alpine AS builder

LABEL org.opencontainers.image.authors="javabackenddeveloperbro@gmail.com"

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle

COPY src src

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -s /bin/sh -D appuser

RUN apk add --no-cache dumb-init

COPY --from=builder /workspace/build/libs/*.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["dumb-init", "java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]