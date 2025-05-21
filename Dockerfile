FROM openjdk:17-jdk-alpine

ARG JAR_FILE=./build/libs/*.jar
COPY ${JAR_FILE} /tmp/

RUN find /tmp -name "*.jar" ! -name "*-plain.jar" -exec mv {} /app.jar \;

ENTRYPOINT ["java", "-jar", "/app.jar"]
