FROM eclipse-temurin:17-alpine
WORKDIR /app
COPY bal-server/target/*-exec.jar app.jar
EXPOSE 8080
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
