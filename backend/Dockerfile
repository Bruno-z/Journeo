# --------- STAGE 1 : BUILD ---------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests


# --------- STAGE 2 : RUNTIME ---------
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Créer un utilisateur non-root
RUN useradd -m springuser

# Copier le jar depuis le builder
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Copier wait-for-it depuis ton repo (RECOMMANDÉ)
COPY scripts/wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# Donner les permissions
RUN chown -R springuser:springuser /app
USER springuser

EXPOSE 8080

CMD ["/wait-for-it.sh", "db:5432", "--", "java", "-jar", "app.jar"]