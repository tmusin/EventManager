FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/eventmanager-0.0.1-SNAPSHOT-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]