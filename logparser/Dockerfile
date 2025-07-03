FROM openjdk:17
WORKDIR /app

# Copy the compiled jar
COPY target/logparser-1.0-SNAPSHOT.jar logparser.jar

ENTRYPOINT ["java", "-jar", "logparser.jar"]
