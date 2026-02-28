CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE guides (
    id BIGSERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    jours INT NOT NULL,
    mobilite VARCHAR(20) NOT NULL,
    saison VARCHAR(20) NOT NULL,
    pour_qui VARCHAR(20) NOT NULL
);

CREATE TABLE activities (
    id BIGSERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    adresse VARCHAR(255),
    telephone VARCHAR(50),
    site_internet VARCHAR(255),
    heure_debut VARCHAR(20),
    duree INT,
    ordre INT,
    jour INT,
    guide_id BIGINT REFERENCES guides(id) ON DELETE CASCADE
);

CREATE TABLE guide_user (
    guide_id BIGINT REFERENCES guides(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (guide_id, user_id)
);