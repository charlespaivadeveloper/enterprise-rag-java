FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -DskipTests package
FROM eclipse-temurin:21-jre
RUN useradd -r -u 10001 app
USER app
COPY --from=build /workspace/target/*.jar /app.jar
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app.jar"]
