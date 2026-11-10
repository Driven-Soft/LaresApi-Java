FROM eclipse-temurin:17-jdk AS build

WORKDIR /workspace
COPY mvnw pom.xml ./
COPY .mvn ./.mvn
COPY src ./src
RUN chmod +x mvnw && ./mvnw -B package

FROM eclipse-temurin:17-jre

WORKDIR /app
RUN useradd --system --uid 10001 --create-home appuser
COPY --from=build /workspace/target/lares-api-0.0.1-SNAPSHOT.jar app.jar
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
