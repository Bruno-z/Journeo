# Journeo

Application web de guides de voyage — Angular 21 + Spring Boot 3 + PostgreSQL.

Les utilisateurs peuvent consulter les guides auxquels ils ont été invités. Les admins gèrent l'ensemble des guides, des activités et des utilisateurs.

---

## Stack

| Couche       | Technologie                              |
|--------------|------------------------------------------|
| Frontend     | Angular 21, standalone components, signals, OnPush |
| Backend      | Spring Boot 3.5, Spring Security, JWT    |
| Base de données | PostgreSQL 15 (prod/Docker), H2 (tests) |
| Migrations   | Flyway 10                                |
| Conteneurs   | Docker + Docker Compose                  |
| Tests        | JUnit 5 / Mockito (205 tests), Jasmine/Karma |

---

## Prérequis

| Outil          | Version minimale | Rôle                              |
|----------------|-----------------|-----------------------------------|
| Docker Desktop | toute version   | Lance PostgreSQL + backend        |
| Node.js        | 20+             | Frontend Angular                  |
| Angular CLI    | `npm i -g @angular/cli` | `ng serve`              |

> Java et Maven ne sont **pas nécessaires** — le backend compile et tourne dans Docker.

---

## Démarrage rapide

```bash
# 1. Cloner le dépôt
git clone https://github.com/Bruno-z/Journeo.git
cd Journeo

# 2. Démarrer la base de données + le backend (compile Spring Boot dans Docker)
docker compose up --build

# 3. Dans un second terminal — démarrer le frontend
cd frontend
npm install
ng serve
```

Ouvrir **http://localhost:4200**

---

## Comptes de test

Créés automatiquement au premier démarrage (DataLoader) :

| Email           | Mot de passe | Rôle          |
|-----------------|-------------|---------------|
| admin@hws.com   | admin123    | Admin         |
| user1@hws.com   | user123     | Utilisateur   |
| user2@hws.com   | user123     | Utilisateur   |

---

## Ce qu'il n'y a PAS à configurer

- Pas de fichier `.env` à créer
- Pas de variable d'environnement à définir (tout est dans `application-dev.properties` et `docker-compose.yml`)
- Pas de base de données à installer manuellement

---

## Problème au démarrage ?

Si `docker compose up` échoue (backend qui démarre avant que PostgreSQL soit prêt) :

```bash
docker compose down -v   # remet tout à zéro (supprime le volume pgdata)
docker compose up --build
```

---

## Lancer les tests

```bash
# Backend (depuis la racine)
cd backend
./mvnw test

# Frontend
cd frontend
ng test
```

---

## Structure du projet

```
Journeo/
├── backend/                    # Spring Boot
│   ├── src/main/java/com/journeo/
│   │   ├── config/             # Sécurité, CORS, DataLoader, MediaStorage
│   │   ├── controller/         # REST controllers
│   │   ├── service/            # Logique métier
│   │   ├── model/              # Entités JPA
│   │   ├── dto/                # Request / Response DTOs
│   │   └── repository/         # Spring Data JPA
│   └── src/main/resources/
│       ├── application.properties
│       ├── application-dev.properties  # JWT secret local
│       └── db/migration/       # Scripts Flyway (V1 → V5)
│
├── frontend/                   # Angular 21
│   └── src/app/
│       ├── core/               # Services, modèles, guards, interceptors
│       ├── features/           # auth, dashboard, guides, users
│       └── shared/             # navbar, sidebar, auth-modal
│
└── docker-compose.yml
```

---

## Fonctionnalités

- Authentification JWT (login / register avec prénom + nom)
- Contrôle d'accès par rôle (ADMIN / USER)
- CRUD guides et activités (admin)
- Upload de photos de couverture pour les guides (media)
- Couverture automatique via l'API Wikipedia (toute ville du monde)
- Commentaires sur les guides
- Géolocalisation des activités
- Interface responsive avec sidebar mobile
- Swagger UI disponible sur `http://localhost:8080/swagger-ui.html`
