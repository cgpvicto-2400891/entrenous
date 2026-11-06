package edu.cegepvicto.comptableplus.controleur;

import edu.cegepvicto.comptableplus.modele.Session;
import edu.cegepvicto.comptableplus.service.AccesRefuseException;
import edu.cegepvicto.comptableplus.service.SessionExpireeException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Fenêtre principale des dossiers clients. Elle contient le point de départ du CU-001-001 :
 * le bouton « Nouveau dossier ». La liste des dossiers (CU-001) viendra dans cette fenêtre.
 */
public class ListeDossiersController extends Fenetre {

    @FXML
    private Label etiquetteUtilisateur;
    @FXML
    private Label etiquetteRefus;
    @FXML
    private Button boutonNouveauDossier;

    /**
     * Crée la fenêtre. Ce constructeur (Stage, DonneesSysteme) est celui qu'attend {@link Fenetre}.
     * @param stage    le Stage à utiliser.
     * @param contexte le contexte de données du système.
     * @throws IOException si le fichier de vue ne peut pas être chargé.
     */
    public ListeDossiersController(Stage stage, DonneesSysteme contexte) throws IOException {
        super(stage, contexte);
    }

    @Override
    protected String getNomFichierVue() {
        return "liste-dossiers-view";
    }

    @Override
    protected String getNomFenetre() {
        return "Les Comptables + — Dossiers clients";
    }

    @Override
    protected int getLargeur() {
        return 520;
    }

    @Override
    protected int getHauteur() {
        return 260;
    }

    @Override
    protected boolean estMaximisee() {
        return false;
    }

    /** Affiche la personne connectée et n'active le bouton que pour un gestionnaire. */
    @FXML
    public void initialize() {
        Session session = contexte.getSession();
        etiquetteUtilisateur.setText("Connecté : " + session.getUtilisateur().nom()
                + " (" + session.getUtilisateur().role().name().toLowerCase() + ")");
        boolean autorise = contexte.getServices().autorisation().peutCreerDossier(session);
        boutonNouveauDossier.setDisable(!autorise);
        etiquetteRefus.setText(autorise ? "" : "Seul un gestionnaire peut créer un dossier client.");
    }

    /**
     * Déclencheur du CU-001-001. Le droit est revérifié ici même si le bouton est actif,
     * car la session peut avoir expiré depuis l'ouverture de la fenêtre.
     */
    @FXML
    private void nouveauDossier() {
        try {
            contexte.getServices().autorisation().verifierCreationDossier(contexte.getSession());
        } catch (SessionExpireeException exception) {
            afficherAlerte(Alert.AlertType.WARNING, exception.getMessage());
            return;
        } catch (AccesRefuseException exception) {
            afficherAlerte(Alert.AlertType.ERROR, exception.getMessage());
            return;
        }
        ouvrirFenetreModale(NouveauDossierController.class);
    }
}
