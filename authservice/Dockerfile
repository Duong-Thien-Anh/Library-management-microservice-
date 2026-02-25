# ─────────────────────────────────────────────────────────────
# STAGE 1: BUILD
# Use the official Maven image with JDK 21 to compile the code
# ─────────────────────────────────────────────────────────────
FROM maven:3.9.6-amazoncorretto-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy pom.xml first to cache Maven dependencies
# (If pom.xml hasn't changed, Docker reuses this layer — faster builds)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ─────────────────────────────────────────────────────────────
# STAGE 2: RUN
# Use a minimal JRE image — no Maven, no source code
# This keeps the final image small (~200MB instead of ~500MB)
# ─────────────────────────────────────────────────────────────
FROM amazoncorretto:21-alpine

WORKDIR /app

# Copy only the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port auth-service runs on
EXPOSE 8082

# Health check so Docker knows when the service is ready
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget -q --spider http://localhost:8081/actuator/health || exit 1

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]