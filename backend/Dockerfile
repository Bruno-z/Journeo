# Utilise une image Java officielle
FROM eclipse-temurin:25-jdk-jammy

# Définit le répertoire de travail
WORKDIR /app

# Copie le pom.xml et télécharge les dépendances Maven
COPY pom.xml .
RUN apt-get update && apt-get install -y maven
RUN mvn dependency:go-offline

# Copie tout le code
COPY src ./src

# Compile et package l'application
RUN mvn clean package -DskipTests

# Expose le port Spring Boot
EXPOSE 8080

# Attendre que la DB soit prête avant de démarrer
# Télécharge wait-for-it.sh
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# Commande de lancement avec attente sur la DB
CMD ["/wait-for-it.sh", "db:5432", "--", "java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]