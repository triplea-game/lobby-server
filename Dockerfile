FROM eclipse-temurin:21

EXPOSE 8080
ADD configuration.yml /configuration.yml
ADD build/libs/lobby-server.jar /lobby-server.jar
CMD java -jar lobby-server.jar server /configuration.yml
