FROM openjdk:21-jdk
LABEL authors="sereg"
ADD target/bankcards-0.0.1-SNAPSHOT.jar bankcards.jar
ENTRYPOINT ["java", "-jar", "/bankcards.jar"]