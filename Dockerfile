# ============================================
# Multi-stage Dockerfile for PDF Rendering Service
# ============================================
# 
# This Dockerfile builds a Spring Boot application with Chinese font support
# Fonts are packaged in the JAR file for reliable container deployment
#
# Usage:
#   docker build -t pdf-service:latest .
#   docker run -p 8080:8080 pdf-service:latest
#

# ============================================
# Stage 1: Build
# ============================================
FROM maven:3.8-openjdk-11-slim AS builder

# Set working directory
WORKDIR /build

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (cached if pom.xml hasn't changed)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application (fonts in src/main/resources/fonts will be packaged in JAR)
RUN mvn clean package -DskipTests

# Verify fonts are in the JAR
RUN jar tf target/*.jar | grep fonts || echo "Warning: No fonts found in JAR"

# ============================================
# Stage 2: Runtime
# ============================================
FROM openjdk:11-jre-slim

# Maintainer information
LABEL maintainer="your-email@example.com"
LABEL description="PDF Rendering Service with Chinese Font Support"

# Set environment variables for UTF-8 support
ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    TZ=Asia/Shanghai

# JVM options
# - Xmx512m: Maximum heap size (adjust based on your needs)
# - java.io.tmpdir: Temporary directory for font extraction
# - Default: /tmp (standard), can be changed to /app/data if preferred
ENV JAVA_OPTS="-Xmx512m -Xms256m -Djava.io.tmpdir=/tmp"

# Create non-root user for security
RUN useradd -m -u 1000 -s /bin/bash appuser

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Create and set permissions for temp directory
# This is where fonts will be extracted from JAR
# Alternative: Use /app/data instead of /tmp (see Dockerfile.jdk8)
RUN mkdir -p /tmp && \
    chmod 777 /tmp && \
    chown appuser:appuser /tmp

# Create directory for debug HTML output (optional)
RUN mkdir -p /app/debug-html && \
    chown appuser:appuser /app/debug-html

# Switch to non-root user
USER appuser

# Health check (Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Alternative with /app/data as temp directory:
# ENV JAVA_OPTS="-Xmx512m -Xms256m -Djava.io.tmpdir=/app/data"
# Alternative CMD for more control:
# CMD ["java", "-Xmx512m", "-Djava.io.tmpdir=/tmp", "-jar", "app.jar"]
