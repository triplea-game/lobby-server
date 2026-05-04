FROM eclipse-temurin:21

EXPOSE 8080
ADD build/libs/lobby-server.jar /lobby-server.jar
CMD java -jar lobby-server.jar
