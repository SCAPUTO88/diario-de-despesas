# BUILD
FROM maven:3.8.1-openjdk-21 AS build
COPY pom.xml /home/app
COPY src /home/app/src
WORKDIR /home/app
RUN mvn clean package -DskipTests

# EXECUÇÃO
FROM openjdk:21-jre-slim
COPY --from=build /home/app/target/*.jar /usr/local/lib/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/lib/app.jar"]


FROM ubuntu:latest
LABEL authors="Sandro Caputo"

ENTRYPOINT ["top", "-b"]