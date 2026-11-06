package edu.cegepvicto.comptableplus;

import edu.cegepvicto.comptableplus.controleur.DonneesSysteme;
import edu.cegepvicto.comptableplus.controleur.ListeDossiersController;
import edu.cegepvicto.comptableplus.modele.Role;
import edu.cegepvicto.comptableplus.modele.Session;
import edu.cegepvicto.comptableplus.modele.Utilisateur;
import edu.cegepvicto.comptableplus.service.LocalisateurServices;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Point d'entrée JavaFX de « Les Comptables + ». Elle crée le localisateur de services et le contexte
 * de données, puis ouvre la première fenêtre.
 * <p>
 * La connexion est une précondition du CU-001-001 : tant que le CU de connexion n'existe pas,
 * une session de gestionnaire est simulée ici.
 */
public class ComptablesPlusApplication extends Application {

    private static final Duration DUREE_SESSION = Duration.ofHours(8);

    @Override
    public void start(Stage stage) throws IOException {
        LocalisateurServices services = LocalisateurServices.depuisEnvironnement();
        Utilisateur gestionnaire = new Utilisateur(1, "Gestionnaire démo", Role.GESTIONNAIRE);
        Clock horloge = Clock.systemDefaultZone();
        Session session = new Session(gestionnaire, Instant.now(horloge).plus(DUREE_SESSION), horloge);

        new ListeDossiersController(stage, new DonneesSysteme(services, session));
    }

    public static void main(String[] args) {
        launch();
    }
}
