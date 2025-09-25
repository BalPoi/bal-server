FROM maven:3.9.11-eclipse-temurin-17-alpine AS build-stage
WORKDIR /app
COPY pom.xml pom.xml
#Make VOLUME for speeding dependency collectioning
RUN mvn dependency:go-offline -B
COPY src src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-alpine
WORKDIR /app
COPY --from=build-stage /app/target/*-shaded.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
