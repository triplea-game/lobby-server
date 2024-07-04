FROM openjdk:11-jre-slim-buster

EXPOSE 8080
ADD configuration.yml /
ADD build/libs/lobby-server.jar /
CMD java -jar lobby-server.jar server /configuration.yml
