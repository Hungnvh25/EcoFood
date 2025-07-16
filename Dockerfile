FROM openjdk:21

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} EcoFood-service.jar

ENTRYPOINT ["java", "-jar", "EcoFood-service.jar"]

EXPOSE 8080