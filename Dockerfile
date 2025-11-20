# ------------------------------------------------------
# Stage 1: Build the JAR with Maven
# ------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy only pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn -q -e -B dependency:resolve dependency:resolve-plugins

# Now copy the full project
COPY src ./src

# Build JAR (skip tests if needed: add -DskipTests)
RUN mvn -q -e -B package -DskipTests


# ------------------------------------------------------
# Stage 2: Create a lightweight runtime image
# ------------------------------------------------------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy jar from Stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose the port Spring Boot will run on
EXPOSE 8080

# Default command
ENTRYPOINT ["java", "-jar", "app.jar"]