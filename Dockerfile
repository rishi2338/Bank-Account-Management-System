FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY northstar-web-app.tar.gz.b64 .
RUN base64 -d northstar-web-app.tar.gz.b64 > northstar-web-app.tar.gz && tar -xzf northstar-web-app.tar.gz
# Add a progressive-enhancement layer without changing banking business logic.
COPY premium-motion/PremiumMotionFilter.java /src/web-app/src/main/java/com/rishi/bankweb/PremiumMotionFilter.java
COPY premium-motion/premium-motion.css /src/web-app/src/main/resources/static/css/premium-motion.css
COPY premium-motion/premium-motion.js /src/web-app/src/main/resources/static/js/premium-motion.js
WORKDIR /src/web-app
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/web-app/target/*.jar app.jar
EXPOSE 8080
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
