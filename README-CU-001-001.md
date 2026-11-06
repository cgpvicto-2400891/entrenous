# CU-001-001 — Créer un dossier client (Les Comptables +)

## Lancer
- Gradle : `gradlew run` (ou lancer `ComptablesPlusApplication` dans IntelliJ avec la tâche Gradle).
- Sans configuration, l'application démarre avec des **données de démonstration en mémoire**
  (clients, types de mandat, un dossier existant pour voir l'avertissement de doublon).
- Avec MySQL : exécuter `src/main/resources/edu/cegepvicto/comptableplus/bd/schema.sql` puis `donnees_demo.sql`,
  et définir les variables d'environnement `COMPTABLES_BD_URL` (ex. `jdbc:mysql://127.0.0.1:3306/comptables`),
  `COMPTABLES_BD_UTILISATEUR` et `COMPTABLES_BD_MOT_DE_PASSE`. Aucun mot de passe n'est dans le code.
- Tests : `gradlew test`.

## Architecture (notes « Choix architecture » et « Diagramme de classe »)
| Élément | Classes |
|---|---|
| MVC – Modèle | `modele/` (`Dossier`, `Client`, `ClientParticulier`, `ClientEntreprise`, `TypeMandat`, `ValidateurDossier`…) |
| MVC – Vue | `controleur/*.fxml` + `styles.css` |
| MVC – Contrôleur | `ListeDossiersController`, `NouveauDossierController` (héritent de `Fenetre`, contexte `DonneesSysteme`) |
| DAO | interfaces `dao/*DAO`, implémentations `dao/sql` (JDBC, requêtes préparées) et `dao/memoire` |
| Localisateur de services | `service/LocalisateurServices` (aucun singleton ni classe statique d'accès) |
| Services | `ServiceDossier` (CU), `ServiceAutorisation` (moindre privilège + journal des refus) |

Principes appliqués : SRP, DRY (validations partagées formulaire/modèle), GRASP Expert en information et Contrôleur,
faible couplage (les fenêtres ne connaissent pas les DAO), encapsulation (modèle immuable qui refuse les données invalides).

## Hors portée (autres CU)
« + Nouveau client » (Créer un client), « + Ajouter le mandat » (Créer le mandat), « Créer et ajouter des tâches »
(CU-002-001) et « … des employés » (CU-001-005) affichent un message « pas encore disponible ».
La connexion est simulée (session de gestionnaire dans `ComptablesPlusApplication`).
