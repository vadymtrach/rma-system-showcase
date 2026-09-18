FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x gradlew

COPY src ./src
# Tests need Docker (Testcontainers), which isn't available during an image build;
# they run in CI (.github/workflows/ci.yml) instead.
RUN ./gradlew bootJar --no-daemon -x test



FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]