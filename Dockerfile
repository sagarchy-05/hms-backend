# Build Stage
# We use a Maven image that includes Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Run Stage
# We use the official Eclipse Temurin Java 21 Alpine image (lightweight)
FROM eclipse-temurin:21-jdk-alpine
COPY --from=build /target/jeevan-hms.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]