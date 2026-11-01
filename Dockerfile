FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/recipe-app-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/src
COPY --from=build /app/src/*.html /app/src/
COPY --from=build /app/src/*.css /app/src/
COPY --from=build /app/src/*.js /app/src/
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
