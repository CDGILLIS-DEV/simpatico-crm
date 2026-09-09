# Build stage
FROM maven:3.9.8-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Package / Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/crm-0.0.1-SNAPSHOT.jar app.jar

# Expose server port (Render binds automatically via $PORT)
EXPOSE 8080

# Run Spring Boot application
ENTRYPOINT ["java", "-Xms256m", "-Xmx768m", "-jar", "app.jar"]
