# Build stage
FROM --platform=linux/amd64 gradle:8-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean build -Pprofile=unit
#RUN gradle clean build -Pprofile=integration

# Package stage
FROM openjdk:21-jdk-slim
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/skch-*.jar /app/app.jar
ENTRYPOINT ["java", "--enable-preview", "-jar", "/app/app.jar"]