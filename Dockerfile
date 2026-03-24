FROM amazoncorretto:21-alpine
WORKDIR /app
COPY target/caseworker-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
