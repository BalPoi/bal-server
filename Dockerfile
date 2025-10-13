FROM maven:3.9.11-eclipse-temurin-17 AS build-stage
WORKDIR /app

#Copying parent pom
COPY pom.xml pom.xml

#Copying modules
COPY bal-server/pom.xml bal-server/pom.xml
COPY bal-server/src bal-server/src

COPY bal-server-api/pom.xml bal-server-api/pom.xml
COPY bal-server-api/src bal-server-api/src

#Building
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-alpine AS run-stage
WORKDIR /app
COPY --from=build-stage /app/bal-server/target/*-exec.jar app.jar
EXPOSE 8080
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
