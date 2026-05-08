FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

# Crear settings.xml con credenciales de GitHub Packages
RUN mkdir -p /root/.m2 && printf '<settings>\n  <servers>\n    <server>\n      <id>github</id>\n      <username>%s</username>\n      <password>%s</password>\n    </server>\n  </servers>\n</settings>\n' "$GITHUB_USERNAME" "$GITHUB_TOKEN" > /root/.m2/settings.xml

COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw mvnw
COPY src src

RUN chmod +x mvnw && ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar jar
COPY --from=builder /app/target/*.jar app.jar

# Permisos
RUN chown appuser:appgroup /app/app.jar
USER appuser

# Puerto dinámico Railway
ENV SERVER_PORT=${PORT}

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:${SERVER_PORT}/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]