FROM amazoncorretto:21-alpine-jdk

EXPOSE 8080
ADD configuration.yml /configuration.yml
ADD build/libs/lobby-server.jar /lobby-server.jar
CMD java -jar lobby-server.jar server /configuration.yml
