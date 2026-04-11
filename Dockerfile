# =============================================================================
# Dockerfile – Demo HR System (Quarkus JVM-Modus)
# =============================================================================
# Multi-Stage Build:
#   Stage 1 (builder) → Maven-Build erzeugt das Quarkus-Artefakt
#   Stage 2 (runtime) → Minimales JRE-Image ohne Build-Tools
#
# Vorteil: Das finale Image enthält keine Maven-Installation oder Sourcen,
# nur das fertige JAR + JRE (~200 MB statt ~800 MB).
# =============================================================================

# ─── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Zuerst nur die pom.xml kopieren und Dependencies laden.
# Docker cached diese Schicht, solange pom.xml unverändert bleibt.
# Dadurch muss Maven bei Code-Änderungen keine Dependencies neu herunterladen.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f pom.xml dependency:go-offline -B

# Dann den Quellcode kopieren und bauen
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f pom.xml package -DskipTests -B

# ─── Stage 2: Runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

# Nicht als root laufen (Security Best Practice)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Quarkus erzeugt ein "Fast JAR" mit separatem lib/-Verzeichnis.
# Die Bibliotheken separat kopieren erlaubt besseres Docker Layer Caching:
# lib/ ändert sich selten → wird gecacht.
# app.jar ändert sich bei jedem Build → neue Schicht.
COPY --from=builder /build/target/quarkus-app/lib/ /app/lib/
COPY --from=builder /build/target/quarkus-app/app/ /app/app/
COPY --from=builder /build/target/quarkus-app/quarkus/ /app/quarkus/
COPY --from=builder /build/target/quarkus-app/quarkus-run.jar /app/quarkus-run.jar

# Port dokumentieren (informativ, kein Port-Forwarding)
EXPOSE 8080

# Gesundheitscheck: Quarkus stellt /q/health bereit (SmallRye Health Extension)
# Hier vereinfacht über den API-Endpunkt
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD wget -q --spider http://localhost:8080/api/employees || exit 1

# Umgebungsvariablen für Datenbankverbindung (können per -e überschrieben werden)
ENV QUARKUS_DATASOURCE_JDBC_URL=jdbc:h2:mem:hrdb;DB_CLOSE_DELAY=-1
ENV QUARKUS_DATASOURCE_USERNAME=sa
ENV QUARKUS_DATASOURCE_PASSWORD=

ENTRYPOINT ["java", "-jar", "/app/quarkus-run.jar"]
