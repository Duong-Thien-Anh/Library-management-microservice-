
FROM maven:3.9.6-amazoncorretto-21 AS build

# Set the working directory inside the container
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

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