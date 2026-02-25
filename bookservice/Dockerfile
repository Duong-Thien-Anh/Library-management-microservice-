
FROM maven:3-eclipse-temurin-21 AS build_stage

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

# Use smaller JRE image (not full JDK) to reduce image size
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Create a non-root user for security
# Running as root is a security risk
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR file from build stage
# --from=build: Copy from previous stage
COPY --from=build_stage /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# Expose port 8082 (matches application.yml)
EXPOSE 8082

# Health check - Docker will periodically check if container is healthy
# If health check fails multiple times, Docker can restart container
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/api/books/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

