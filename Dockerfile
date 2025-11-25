# ------------------------------------------------------
# Stage 1: Build the JAR with Maven Wrapper
# ------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml first (best for caching)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Pre-download Maven dependencies
RUN ./mvnw -q -e -B dependency:go-offline

# Copy the rest of the project
COPY src ./src

# Build the application
RUN ./mvnw -q -e -B clean package -DskipTests


# ------------------------------------------------------
# Stage 2: Create a lightweight runtime image
# ------------------------------------------------------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy final JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# Optional optimized JVM flags for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
