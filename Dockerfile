FROM amazoncorretto:21-alpine3.19 AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew build -x test

FROM amazoncorretto:21-alpine3.19
ENV TZ=America/Sao_Paulo
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]
