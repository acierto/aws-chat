FROM openjdk:11-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} aws-chat-server.jar
ENTRYPOINT ["java","-jar","/aws-chat-server.jar"]
