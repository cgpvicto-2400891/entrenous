-- Modèle de données du CU-001-001 « Créer un dossier client » (Les Comptables +).
-- Compatible MySQL 8. Exécuter une seule fois sur une base vide.

CREATE TABLE CategorieClient (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    code    VARCHAR(20)  NOT NULL UNIQUE,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE Client (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    categorie_id INT          NOT NULL,
    courriel     VARCHAR(150) NOT NULL,
    telephone    VARCHAR(20)  NOT NULL,
    adresse      VARCHAR(255) NOT NULL,
    FOREIGN KEY (categorie_id) REFERENCES CategorieClient (id)
);

CREATE TABLE ClientParticulier (
    client_id INT          PRIMARY KEY,
    prenom    VARCHAR(100) NOT NULL,
    nom       VARCHAR(100) NOT NULL,
    FOREIGN KEY (client_id) REFERENCES Client (id)
);

CREATE TABLE ClientEntreprise (
    client_id          INT          PRIMARY KEY,
    nom_entreprise     VARCHAR(150) NOT NULL,
    numero_entreprise  VARCHAR(20)  NULL,
    personne_ressource VARCHAR(100) NOT NULL,
    FOREIGN KEY (client_id) REFERENCES Client (id)
);

CREATE TABLE TypeMandat (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    nom                     VARCHAR(100) NOT NULL,
    description_obligatoire BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE TypeMandatCategorie (
    type_mandat_id INT NOT NULL,
    categorie_id   INT NOT NULL,
    PRIMARY KEY (type_mandat_id, categorie_id),
    FOREIGN KEY (type_mandat_id) REFERENCES TypeMandat (id),
    FOREIGN KEY (categorie_id) REFERENCES CategorieClient (id)
);

-- gestionnaire_id vient de la base de données centrale : pas de clé étrangère locale.
-- Aucun DELETE sur Dossier : le statut passe à « Archivé » (conservation de 7 ans).
-- Le doublon (client, type, période) est vérifié par l'application, sans contrainte UNIQUE.
CREATE TABLE Dossier (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    numero          VARCHAR(9)   NOT NULL UNIQUE,
    date_ouverture  DATE         NOT NULL,
    statut          VARCHAR(10)  NOT NULL,
    periode_visee   SMALLINT     NOT NULL,
    description     VARCHAR(500) NOT NULL,
    client_id       INT          NOT NULL,
    type_mandat_id  INT          NOT NULL,
    gestionnaire_id INT          NOT NULL,
    FOREIGN KEY (client_id) REFERENCES Client (id),
    FOREIGN KEY (type_mandat_id) REFERENCES TypeMandat (id)
);

CREATE TABLE Journal (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    date_heure     TIMESTAMP   NOT NULL,
    utilisateur_id INT         NOT NULL,
    action         VARCHAR(50) NOT NULL,
    dossier_id     INT         NULL,
    FOREIGN KEY (dossier_id) REFERENCES Dossier (id)
);

-- Données de référence
INSERT INTO CategorieClient (code, libelle) VALUES ('PARTICULIER', 'Particulier'), ('ENTREPRISE', 'Entreprise');

INSERT INTO TypeMandat (nom, description_obligatoire) VALUES
    ('Déclaration de revenus', FALSE),
    ('Tenue de livres', FALSE),
    ('Déclaration de société', FALSE),
    ('Demande de permis', FALSE),
    ('Autre (sur mesure)', TRUE);

INSERT INTO TypeMandatCategorie (type_mandat_id, categorie_id) VALUES
    (1, 1), (2, 1), (4, 1), (5, 1),
    (2, 2), (3, 2), (4, 2), (5, 2);
