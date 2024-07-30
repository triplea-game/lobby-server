FROM openjdk:17-jdk-slim

EXPOSE 8080
ADD configuration.yml /
ADD build/libs/lobby-server.jar /
CMD java -jar lobby-server.jar server /configuration.yml
