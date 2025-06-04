FROM openjdk:17-jdk-alpine

COPY build/libs/*.jar /tmp/

RUN find /tmp -name "*.jar" ! -name "*-plain.jar" -exec mv {} /app.jar \; && \
    rm -rf /tmp/*

ENTRYPOINT ["java", "-jar", "/app.jar"]