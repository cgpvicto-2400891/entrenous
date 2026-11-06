package edu.cegepvicto.comptableplus.controleur;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Super classe de tous les contrôleurs de fenêtre. Elle charge la vue FXML, l'affiche dans un
 * {@link Stage} et transmet le contexte de données du système.
 * <p>
 * Attention : le constructeur charge la vue, donc {@code initialize()} du contrôleur s'exécute avant la
 * fin du constructeur des sous-classes. Les sous-classes n'utilisent donc pas d'initialiseur de champ
 * (ils seraient exécutés après {@code initialize()}) : tout est préparé dans {@code initialize()}.
 * Le fichier FXML ne déclare pas {@code fx:controller} : le contrôleur est assigné par le code.
 */
public abstract class Fenetre {

    private static final String CLASSE_STYLE_ERREUR = "erreur";

    private final Logger journaliseur = Logger.getLogger(getClass().getName());

    /** Le stage dans lequel la fenêtre s'affiche. */
    protected Stage stageUtilise;

    /** Le contexte de données du système. */
    protected DonneesSysteme contexte;

    /** @return le nom du fichier FXML, sans extension, situé dans le même dossier que le contrôleur. */
    protected abstract String getNomFichierVue();

    /** @return le titre de la fenêtre. */
    protected abstract String getNomFenetre();

    /** @return la largeur initiale de la fenêtre. */
    protected abstract int getLargeur();

    /** @return la hauteur initiale de la fenêtre. */
    protected abstract int getHauteur();

    /** @return {@code true} si la fenêtre s'ouvre maximisée. */
    protected abstract boolean estMaximisee();

    /**
     * Crée une fenêtre qui s'affiche dans un Stage déjà existant.
     * @param stage    le Stage à utiliser, ou {@code null} pour en créer un nouveau.
     * @param contexte le contexte de données du système.
     * @throws IOException si le fichier de vue ne peut pas être chargé.
     */
    public Fenetre(Stage stage, DonneesSysteme contexte) throws IOException {
        if (stage == null) {
            stage = new Stage();
        }
        stageUtilise = stage;
        this.contexte = contexte;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(getNomFichierVue() + ".fxml"));
        fxmlLoader.setController(this);
        Scene scene = new Scene(fxmlLoader.load(), getLargeur(), getHauteur());

        stage.setMaximized(estMaximisee());
        stage.setTitle(getNomFenetre());
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Ouvre une nouvelle fenêtre liée au contrôleur spécifié.
     * @param controleur le contrôleur du nouveau contenu à afficher.
     * @return l'instance du contrôleur créé, ou {@code null} si la fenêtre n'a pas pu s'ouvrir.
     */
    protected Fenetre ouvrirFenetre(Class<? extends Fenetre> controleur) {
        return chargerFenetre(controleur, null);
    }

    /**
     * Ouvre une fenêtre modale : la fenêtre actuelle reste bloquée tant qu'elle n'est pas fermée.
     * @param controleur le contrôleur du nouveau contenu à afficher.
     * @return l'instance du contrôleur créé, ou {@code null} si la fenêtre n'a pas pu s'ouvrir.
     */
    protected Fenetre ouvrirFenetreModale(Class<? extends Fenetre> controleur) {
        Stage stageModal = new Stage();
        stageModal.initModality(Modality.APPLICATION_MODAL);
        stageModal.initOwner(stageUtilise);
        return chargerFenetre(controleur, stageModal);
    }

    /**
     * Remplace le contenu de la fenêtre avec celui lié au contrôleur spécifié.
     * @param controleur le contrôleur du nouveau contenu à afficher.
     * @return l'instance du contrôleur créé, ou {@code null} si la fenêtre n'a pas pu s'ouvrir.
     */
    protected Fenetre remplacerFenetre(Class<? extends Fenetre> controleur) {
        return chargerFenetre(controleur, stageUtilise);
    }

    /**
     * Charge une fenêtre et l'affiche. Si elle ne peut pas être chargée, la cause technique est
     * consignée dans les logs (jamais affichée) et la personne reçoit un message général.
     */
    private Fenetre chargerFenetre(Class<? extends Fenetre> controleur, Stage stage) {
        try {
            Constructor<? extends Fenetre> constructeur =
                    controleur.getConstructor(Stage.class, DonneesSysteme.class);
            return constructeur.newInstance(stage, contexte);
        } catch (InvocationTargetException exceptionControleur) {
            journaliseur.log(Level.SEVERE, "Exception lors du chargement de la fenêtre "
                    + controleur.getSimpleName() + " (fichier de vue introuvable ou erreur dans le contrôleur).",
                    exceptionControleur.getCause());
        } catch (NoSuchMethodException | SecurityException exceptionControleur) {
            journaliseur.log(Level.SEVERE, "Le contrôleur " + controleur.getSimpleName()
                    + " n'a pas de constructeur (Stage, DonneesSysteme).", exceptionControleur);
        } catch (InstantiationException | IllegalArgumentException | IllegalAccessException exceptionCreation) {
            journaliseur.log(Level.SEVERE, "Impossible d'instancier le contrôleur "
                    + controleur.getSimpleName() + ".", exceptionCreation);
        }
        afficherAlerte(Alert.AlertType.ERROR, "La fenêtre demandée n'a pas pu être ouverte.");
        return null;
    }

    /**
     * Affiche un message dans une boîte de dialogue au-dessus de la fenêtre.
     * @param type    le type de boîte (information, avertissement, erreur).
     * @param message le message affiché, sans détails techniques.
     */
    protected void afficherAlerte(Alert.AlertType type, String message) {
        Alert alerte = new Alert(type, message, ButtonType.OK);
        alerte.initOwner(stageUtilise);
        alerte.setHeaderText(null);
        alerte.showAndWait();
    }

    /**
     * Pose une question à deux réponses dans une boîte de dialogue.
     * @param message le texte de la question.
     * @param accepter le bouton qui représente la réponse « oui ».
     * @param refuser  le bouton qui représente la réponse « non ».
     * @return {@code true} si la personne a choisi le bouton {@code accepter}.
     */
    protected boolean confirmer(String message, ButtonType accepter, ButtonType refuser) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, message, accepter, refuser);
        confirmation.initOwner(stageUtilise);
        confirmation.setHeaderText(null);
        return confirmation.showAndWait().filter(bouton -> bouton == accepter).isPresent();
    }

    /**
     * Affiche (ou retire) le message d'erreur d'un champ et ajuste son style.
     * @param champ    le contrôle validé (reçoit la classe de style « erreur »).
     * @param etiquette l'étiquette qui affiche le message sous le champ.
     * @param message  le message d'erreur, ou une chaîne vide si la valeur est valide.
     * @return {@code true} si la valeur est valide (aucun message).
     */
    protected boolean afficherValidation(Node champ, Label etiquette, String message) {
        boolean valide = message.isEmpty();
        etiquette.setText(message);
        if (valide) {
            champ.getStyleClass().remove(CLASSE_STYLE_ERREUR);
        } else if (!champ.getStyleClass().contains(CLASSE_STYLE_ERREUR)) {
            champ.getStyleClass().add(CLASSE_STYLE_ERREUR);
        }
        return valide;
    }
}
