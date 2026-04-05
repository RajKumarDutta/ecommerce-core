# --------------------------
# Stage 1: Dependencies (cache)
# --------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS deps
WORKDIR /app

COPY pom.xml .

# Cache dependencies (only re-run if pom.xml changes)
RUN mvn dependency:go-offline -B

# --------------------------
# Stage 2: Build
# --------------------------
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Reuse cached dependencies
COPY --from=deps /root/.m2 /root/.m2

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# --------------------------
# Stage 3: Runtime
# --------------------------
FROM eclipse-temurin:17-jre-jammy AS runner
WORKDIR /app

# Copy only the final jar
COPY --from=builder /app/target/*.jar app.jar

LABEL authors="rdutta"

# Optional: run as non-root (better security)
RUN useradd -m appuser
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]