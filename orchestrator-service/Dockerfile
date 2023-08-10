FROM openjdk:17-jdk-alpine

RUN mkdir -p /opt

COPY build/libs/*.jar /opt/executable.jar

ENTRYPOINT ["java","-jar","/opt/executable.jar"]