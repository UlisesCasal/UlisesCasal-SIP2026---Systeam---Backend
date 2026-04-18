# Build stage - Maven para compilar
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY backend/pom.xml ./
COPY backend/.mvn .mvn
COPY backend/mvnw mvnw
COPY backend/src src

RUN chmod +x mvnw && ./mvnw -q -DskipTests package

# Runtime stage - JRE slim para ejecutar
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario no-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar JAR desde build stage
COPY --from=builder /app/target/*.jar app.jar

# Permisos para usuario no-root
RUN chown appuser:appgroup /app/app.jar
USER appuser

# Puerto dinámico según variable de entorno de Railway
ENV SERVER_PORT=${PORT:-8080}
EXPOSE 8080

# Healthcheck hacia actuator
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# ENTRYPOINT en forma exec (array JSON) para signals limpias
ENTRYPOINT ["java", "-Dserver.port=${SERVER_PORT}", "-jar", "/app/app.jar"]