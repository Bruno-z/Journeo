# Étape 1 : build Maven + JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn clean compile package -DskipTests --fail-at-end

# Étape 2 : runtime JDK 17
FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]