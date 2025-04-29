FROM openjdk:17-jdk-slim

EXPOSE 8080
CMD java -jar lobby-server.jar server /configuration.yml
