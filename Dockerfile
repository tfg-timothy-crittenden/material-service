# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom first to leverage Docker layer cache
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# Pre-download dependencies
RUN mvn -q -DskipTests dependency:go-offline

# Copy source and build
COPY src src
RUN mvn -q -DskipTests clean package

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Service runs on 8082
EXPOSE 8082

# Defaults for Docker network (override if needed)
ENV SERVER_PORT=8082
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/classroom_db
ENV SPRING_DATASOURCE_USERNAME=myuser
ENV SPRING_DATASOURCE_PASSWORD=secret

ENTRYPOINT ["java", "-jar", "/app/app.jar"]