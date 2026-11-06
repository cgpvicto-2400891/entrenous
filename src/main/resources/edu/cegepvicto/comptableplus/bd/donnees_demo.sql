-- Clients de démonstration (le CU « Créer un client » n'est pas encore implémenté).
-- À exécuter après schema.sql.

INSERT INTO Client (categorie_id, courriel, telephone, adresse) VALUES
    (1, 'marie.tremblay@exemple.ca', '819 555-0101', '12, rue des Érables, Victoriaville'),
    (1, 'jean.gagnon@exemple.ca', '819 555-0102', '45, boulevard Bois-Francs, Victoriaville'),
    (1, 'sophie.lavoie@exemple.ca', '819 555-0103', '8, rue Notre-Dame, Warwick'),
    (2, 'info@boulangeriedupuis.ca', '819 555-0201', '100, rue Principale, Victoriaville'),
    (2, 'admin@constructionbeauce.ca', '819 555-0202', '250, route 116, Princeville'),
    (2, 'garage.belanger@exemple.ca', '819 555-0203', '3, rue du Garage, Kingsey Falls');

INSERT INTO ClientParticulier (client_id, prenom, nom) VALUES
    (1, 'Marie', 'Tremblay'), (2, 'Jean', 'Gagnon'), (3, 'Sophie', 'Lavoie');

INSERT INTO ClientEntreprise (client_id, nom_entreprise, numero_entreprise, personne_ressource) VALUES
    (4, 'Boulangerie Dupuis inc.', '1234567890', 'Luc Dupuis'),
    (5, 'Construction Beauce ltée', '9876543210', 'Isabelle Roy'),
    (6, 'Garage Bélanger', NULL, 'Paul Bélanger');
