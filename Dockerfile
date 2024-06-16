FROM maven:3.9.6-amazoncorretto-21

ENV TZ=America/Sao_Paulo

COPY . .

RUN ./gradlew build

EXPOSE 8080

CMD ["java", "-jar", "build/libs/gateway-0.0.1-SNAPSHOT.jar"]
