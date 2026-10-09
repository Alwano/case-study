FROM ubuntu:latest
LABEL authors="akemboi"
WORKDIR /app

COPY target/country-info-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["top", "-b"]