# ── Stage 1: Build ─────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-11-alpine AS builder

WORKDIR /build

# Copiar pom.xml e baixar dependências primeiro (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código e compilar
COPY src ./src
RUN mvn package -DskipTests -B

# ── Stage 2: Runtime ────────────────────────────────────────────
FROM eclipse-temurin:11-jre-alpine

# Instalar curl para healthcheck
RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Africa/Luanda /etc/localtime && \
    echo "Africa/Luanda" > /etc/timezone

WORKDIR /app

# Copiar JAR gerado
COPY --from=builder /build/target/faturacao-*.jar app.jar

# Criar directório para relatórios
RUN mkdir -p /app/reports

# Utilizador não-root por segurança
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

# Opções JVM optimizadas para container
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
