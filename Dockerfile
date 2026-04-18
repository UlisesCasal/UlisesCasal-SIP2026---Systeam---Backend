# Buildstage
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY backend/pom.xml ./
COPY backend/.mvn .mvn
COPY backend/mvnw mvnw
COPY backend/src src

RUN chmod +x mvnw && ./mvnw -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/*.jar app.jar

RUN chown appuser:appgroup /app/app.jar
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Dserver.port=8080", "-jar", "/app/app.jar"]