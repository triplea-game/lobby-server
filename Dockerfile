FROM eclipse-temurin:21

EXPOSE 8080

WORKDIR /app
COPY build/quarkus-app/lib/ lib/
COPY build/quarkus-app/app/ app/
COPY build/quarkus-app/quarkus/ quarkus/
COPY build/quarkus-app/quarkus-run.jar quarkus-run.jar

CMD ["java", "-jar", "quarkus-run.jar"]
