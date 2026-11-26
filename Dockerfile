FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY northstar-web-app.tar.gz.b64 .
RUN base64 -d northstar-web-app.tar.gz.b64 > northstar-web-app.tar.gz && tar -xzf northstar-web-app.tar.gz
WORKDIR /src/web-app
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/web-app/target/*.jar app.jar
EXPOSE 8080
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
