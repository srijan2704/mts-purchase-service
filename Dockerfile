# syntax=docker/dockerfile:1

# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy only pom first for better layer caching
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# Copy source and build jar
COPY src ./src
RUN mvn -q clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy fat jar from build stage
COPY --from=build /workspace/target/*.jar /app/app.jar

# Render provides PORT; fallback to 8080 for local docker runs
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar /app/app.jar"]
