# Stage 1: Build application with Maven
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and POM
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build production jar
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal JRE Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Pre-create storage directory and assign ownership to appuser
RUN mkdir -p /app/storage/resumes && chown -R appuser:appgroup /app

USER appuser

# Copy jar from builder
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

# Configure default port (overridden dynamically by Render via PORT env var)
ENV PORT=8080
ENV RESUME_STORAGE_DIR=/app/storage/resumes
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
