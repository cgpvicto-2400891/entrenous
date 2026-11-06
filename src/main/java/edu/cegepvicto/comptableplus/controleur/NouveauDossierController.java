package edu.cegepvicto.comptableplus.controleur;

import edu.cegepvicto.comptableplus.dao.DonneesException;
import edu.cegepvicto.comptableplus.modele.Client;
import edu.cegepvicto.comptableplus.modele.Dossier;
import edu.cegepvicto.comptableplus.modele.Session;
import edu.cegepvicto.comptableplus.modele.TypeMandat;
import edu.cegepvicto.comptableplus.modele.ValidateurDossier;
import edu.cegepvicto.comptableplus.service.AccesRefuseException;
import edu.cegepvicto.comptableplus.service.ServiceDossier;
import edu.cegepvicto.comptableplus.service.SessionExpireeException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Fenêtre « Nouveau dossier client » : cas d'utilisation CU-001-001.
 * <p>
 * Rôle MVC : contrôleur. Il valide les champs (avec les règles de {@link ValidateurDossier}), appelle
 * le {@link ServiceDossier} et met la vue à jour. Il ne connaît ni la base de données ni les DAO.
 * <p>
 * Le bouton « Créer le dossier » n'est actif que lorsque tous les champs sont valides. Les messages
 * d'erreur s'affichent sous le champ concerné à la perte de focus (et en direct pour la description).
 * Aucun champ n'a d'initialiseur : tout est préparé dans {@link #initialize()} (voir {@link Fenetre}).
 */
public class NouveauDossierController extends Fenetre {

    private static final int LONGUEUR_MIN_RECHERCHE = 2;
    private static final int NOMBRE_ANNEES_PROPOSEES = 10;
    private static final String TEXTE_RESUME_VIDE =
            "Résumé du client sélectionné : nom, catégorie (Particulier / Entreprise), coordonnées, "
                    + "nombre de dossiers existants.";
    private static final String TITRE_DESCRIPTION = "Description du dossier";

    @FXML
    private HBox bandeauErreur;
    @FXML
    private Label messageBandeau;
    @FXML
    private TextField champNumero;
    @FXML
    private DatePicker choixDateOuverture;
    @FXML
    private Label erreurDateOuverture;
    @FXML
    private TextField champGestionnaire;
    @FXML
    private TextField champRechercheClient;
    @FXML
    private ListView<Client> resultatsClients;
    @FXML
    private Label erreurClient;
    @FXML
    private Label resumeClient;
    @FXML
    private ComboBox<TypeMandat> choixTypeMandat;
    @FXML
    private Button boutonAjouterMandat;
    @FXML
    private Label indiceTypeMandat;
    @FXML
    private Label erreurTypeMandat;
    @FXML
    private ComboBox<Integer> choixPeriodeVisee;
    @FXML
    private Label erreurPeriodeVisee;
    @FXML
    private Label etiquetteDescription;
    @FXML
    private TextArea zoneDescription;
    @FXML
    private Label erreurDescription;
    @FXML
    private Label compteurDescription;
    @FXML
    private Button boutonCreer;
    @FXML
    private Button boutonCreerTaches;
    @FXML
    private Button boutonCreerEmployes;

    // État du formulaire (affecté dans initialize)
    private ServiceDossier service;
    private ValidateurDossier validateur;
    private Session session;
    private DateTimeFormatter formatDate;
    private Client clientChoisi;
    private boolean numeroCharge;
    private boolean miseAJourInterne;

    /**
     * Crée la fenêtre. Ce constructeur (Stage, DonneesSysteme) est celui qu'attend {@link Fenetre}.
     * @param stage    le Stage à utiliser.
     * @param contexte le contexte de données du système.
     * @throws IOException si le fichier de vue ne peut pas être chargé.
     */
    public NouveauDossierController(Stage stage, DonneesSysteme contexte) throws IOException {
        super(stage, contexte);
    }

    @Override
    protected String getNomFichierVue() {
        return "nouveau-dossier-view";
    }

    @Override
    protected String getNomFenetre() {
        return "Nouveau dossier client";
    }

    @Override
    protected int getLargeur() {
        return 700;
    }

    @Override
    protected int getHauteur() {
        return 780;
    }

    @Override
    protected boolean estMaximisee() {
        return false;
    }

    // ------------------------------------------------------------------ initialisation

    /** Prépare les contrôles, les observateurs et les valeurs par défaut du formulaire. */
    @FXML
    public void initialize() {
        service = contexte.getServices().dossiers();
        validateur = contexte.getServices().validateur();
        session = contexte.getSession();
        formatDate = DateTimeFormatter.ISO_LOCAL_DATE;
        numeroCharge = false;
        miseAJourInterne = false;

        champGestionnaire.setText(session.getUtilisateur().nom());
        resumeClient.setText(TEXTE_RESUME_VIDE);

        preparerDate();
        preparerPeriode();
        preparerRechercheClient();
        preparerTypeMandat();
        preparerDescription();

        // Fermer la fenêtre avec la croix suit les mêmes règles que le bouton « Annuler ».
        stageUtilise.setOnCloseRequest(evenement -> {
            if (!confirmerAbandon()) {
                evenement.consume();
            }
        });

        chargerNumeroPrevu();
        mettreAJourBoutons();
    }

    private void preparerDate() {
        choixDateOuverture.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : date.format(formatDate);
            }

            @Override
            public LocalDate fromString(String texte) {
                return texte == null || texte.isBlank() ? null : LocalDate.parse(texte.strip(), formatDate);
            }
        });
        choixDateOuverture.setValue(LocalDate.now());
        choixDateOuverture.getEditor().textProperty().addListener((observable, ancien, nouveau) -> mettreAJourBoutons());
        choixDateOuverture.getEditor().focusedProperty().addListener((observable, ancien, nouveau) -> {
            if (!nouveau) {
                validerDateOuverture();
                mettreAJourBoutons();
            }
        });
    }

    private void preparerPeriode() {
        int anneeCourante = LocalDate.now().getYear();
        ObservableList<Integer> annees = FXCollections.observableArrayList();
        for (int annee = anneeCourante; annee > anneeCourante - NOMBRE_ANNEES_PROPOSEES; annee--) {
            annees.add(annee);
        }
        choixPeriodeVisee.setItems(annees);
        choixPeriodeVisee.setValue(anneeCourante - 1);   // l'année financière la plus souvent visée
        choixPeriodeVisee.valueProperty().addListener((observable, ancien, nouveau) -> mettreAJourBoutons());
    }

    private void preparerRechercheClient() {
        champRechercheClient.textProperty().addListener((observable, ancien, nouveau) -> {
            if (miseAJourInterne) {
                return;
            }
            if (clientChoisi != null) {
                reinitialiserClient();   // modifier le texte annule le client choisi
            }
            rechercherClients(nouveau);
            mettreAJourBoutons();
        });
        champRechercheClient.focusedProperty().addListener((observable, ancien, nouveau) -> {
            // Pendant le choix dans la liste des résultats, le champ perd le focus : pas encore d'erreur.
            if (!nouveau && !resultatsClients.isVisible()) {
                validerClient();
            }
        });
        // Entrée choisit le premier résultat; la flèche du bas passe dans la liste (clavier seulement).
        champRechercheClient.setOnAction(evenement -> {
            if (resultatsClients.isVisible() && !resultatsClients.getItems().isEmpty()) {
                choisirClient(resultatsClients.getItems().get(0));
            }
        });
        champRechercheClient.setOnKeyPressed(evenement -> {
            if (evenement.getCode() == KeyCode.DOWN && resultatsClients.isVisible()) {
                resultatsClients.requestFocus();
                resultatsClients.getSelectionModel().selectFirst();
            }
        });
        resultatsClients.setOnMouseClicked(evenement -> choisirClientSelectionne());
        resultatsClients.setOnKeyPressed(evenement -> {
            if (evenement.getCode() == KeyCode.ENTER) {
                choisirClientSelectionne();
            }
        });
    }

    private void preparerTypeMandat() {
        choixTypeMandat.setDisable(true);
        boutonAjouterMandat.setDisable(true);
        choixTypeMandat.valueProperty().addListener((observable, ancien, nouveau) -> {
            boolean obligatoire = nouveau != null && nouveau.descriptionObligatoire();
            etiquetteDescription.setText(TITRE_DESCRIPTION + (obligatoire ? " *" : ""));
            erreurTypeMandat.setText("");
            choixTypeMandat.getStyleClass().remove("erreur");
            mettreAJourBoutons();
        });
        choixTypeMandat.focusedProperty().addListener((observable, ancien, nouveau) -> {
            if (!nouveau && !choixTypeMandat.isDisabled()) {
                validerTypeMandat();
            }
        });
    }

    private void preparerDescription() {
        zoneDescription.textProperty().addListener((observable, ancien, nouveau) -> {
            int longueur = nouveau.length();
            compteurDescription.setText(longueur + " / " + ValidateurDossier.LONGUEUR_MAX_DESCRIPTION);
            validerDescription();
            mettreAJourBoutons();
        });
    }

    // ------------------------------------------------------------------ chargement des données

    /** Charge le numéro prévu. Si les données sont inaccessibles, le bandeau propose de réessayer. */
    private void chargerNumeroPrevu() {
        try {
            champNumero.setText(service.numeroPrevu());
            numeroCharge = true;
            masquerBandeau();
        } catch (DonneesException exception) {
            numeroCharge = false;
            champNumero.setText("");
            afficherBandeau(exception.getMessage());
        }
    }

    private void rechercherClients(String texte) {
        String recherche = texte == null ? "" : texte.strip();
        if (recherche.length() < LONGUEUR_MIN_RECHERCHE) {
            masquerResultats();
            erreurClient.setText("");
            return;
        }
        try {
            List<Client> clients = service.rechercherClients(recherche);
            resultatsClients.setItems(FXCollections.observableArrayList(clients));
            boolean aucun = clients.isEmpty();
            resultatsClients.setVisible(!aucun);
            resultatsClients.setManaged(!aucun);
            erreurClient.setText(aucun ? "Aucun client trouvé. Utilisez « + Nouveau client »." : "");
        } catch (DonneesException exception) {
            masquerResultats();
            erreurClient.setText(exception.getMessage());
        }
    }

    private void masquerResultats() {
        resultatsClients.setVisible(false);
        resultatsClients.setManaged(false);
        resultatsClients.getItems().clear();
    }

    private void choisirClientSelectionne() {
        Client selection = resultatsClients.getSelectionModel().getSelectedItem();
        if (selection != null) {
            choisirClient(selection);
        }
    }

    /** Sélectionne un client : affiche son résumé et charge les types de mandat de sa catégorie. */
    private void choisirClient(Client client) {
        clientChoisi = client;
        miseAJourInterne = true;
        champRechercheClient.setText(client.getNomAffichage());
        miseAJourInterne = false;
        masquerResultats();
        afficherValidation(champRechercheClient, erreurClient, ValidateurDossier.VALIDE);
        try {
            int nombreDossiers = service.nombreDossiers(client);
            resumeClient.setText(client.getNomAffichage() + " — " + client.getCategorie().libelle() + "\n"
                    + client.getCoordonnees() + "\n"
                    + nombreDossiers + (nombreDossiers > 1 ? " dossiers existants" : " dossier existant"));
            choixTypeMandat.setItems(FXCollections.observableArrayList(service.typesMandatOffertsA(client)));
            choixTypeMandat.setDisable(false);
            boutonAjouterMandat.setDisable(false);
            indiceTypeMandat.setVisible(false);
            indiceTypeMandat.setManaged(false);
            masquerBandeau();
        } catch (DonneesException exception) {
            afficherBandeau(exception.getMessage());
        }
        mettreAJourBoutons();
    }

    private void reinitialiserClient() {
        clientChoisi = null;
        resumeClient.setText(TEXTE_RESUME_VIDE);
        choixTypeMandat.getItems().clear();
        choixTypeMandat.setValue(null);
        choixTypeMandat.setDisable(true);
        boutonAjouterMandat.setDisable(true);
        indiceTypeMandat.setVisible(true);
        indiceTypeMandat.setManaged(true);
    }

    // ------------------------------------------------------------------ validation

    private boolean validerClient() {
        return afficherValidation(champRechercheClient, erreurClient, validateur.validerClient(clientChoisi));
    }

    private boolean validerTypeMandat() {
        return afficherValidation(choixTypeMandat, erreurTypeMandat,
                validateur.validerTypeMandat(choixTypeMandat.getValue()));
    }

    private boolean validerDateOuverture() {
        return afficherValidation(choixDateOuverture, erreurDateOuverture, messageDateOuverture());
    }

    private boolean validerPeriodeVisee() {
        return afficherValidation(choixPeriodeVisee, erreurPeriodeVisee,
                validateur.validerPeriodeVisee(choixPeriodeVisee.getValue(), LocalDate.now().getYear()));
    }

    private boolean validerDescription() {
        return afficherValidation(zoneDescription, erreurDescription,
                validateur.validerDescription(zoneDescription.getText(), choixTypeMandat.getValue()));
    }

    /** Valide tous les champs (et affiche tous les messages, sans s'arrêter au premier). */
    private boolean validerTout() {
        boolean valide = validerClient();
        valide &= validerTypeMandat();
        valide &= validerDateOuverture();
        valide &= validerPeriodeVisee();
        valide &= validerDescription();
        return valide;
    }

    /** Vérifie les champs sans afficher de message : sert à activer ou non les boutons de création. */
    private boolean formulaireValide() {
        return numeroCharge
                && ValidateurDossier.VALIDE.equals(validateur.validerClient(clientChoisi))
                && ValidateurDossier.VALIDE.equals(validateur.validerTypeMandat(choixTypeMandat.getValue()))
                && ValidateurDossier.VALIDE.equals(messageDateOuverture())
                && ValidateurDossier.VALIDE.equals(validateur.validerPeriodeVisee(choixPeriodeVisee.getValue(),
                        LocalDate.now().getYear()))
                && ValidateurDossier.VALIDE.equals(validateur.validerDescription(zoneDescription.getText(),
                        choixTypeMandat.getValue()));
    }

    private void mettreAJourBoutons() {
        boolean desactiver = !formulaireValide();
        boutonCreer.setDisable(desactiver);
        boutonCreerTaches.setDisable(desactiver);
        boutonCreerEmployes.setDisable(desactiver);
    }

    /** Message d'erreur de la date, calculé à partir du texte tel que saisi dans le champ. */
    private String messageDateOuverture() {
        LocalDate date = lireDateOuverture();
        return validateur.validerDateOuverture(date, LocalDate.now());
    }

    /** @return la date saisie, ou {@code null} si le champ est vide ou n'est pas une date valide. */
    private LocalDate lireDateOuverture() {
        try {
            return choixDateOuverture.getConverter().fromString(choixDateOuverture.getEditor().getText());
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    // ------------------------------------------------------------------ actions (boutons)

    @FXML
    private void creerDossier() {
        creer(null);
    }

    @FXML
    private void creerEtAjouterTaches() {
        creer("Le cas d'utilisation CU-002-001 « Ajouter une tâche à un dossier » n'est pas encore disponible.");
    }

    @FXML
    private void creerEtAjouterEmployes() {
        creer("Le cas d'utilisation CU-001-005 « Ajouter un employé à un dossier » n'est pas encore disponible.");
    }

    /**
     * Crée le dossier (scénario nominal, étape finale).
     * @param messageSuite message affiché après la confirmation quand la suite est un autre CU, sinon {@code null}.
     */
    private void creer(String messageSuite) {
        if (!validerTout()) {
            return;
        }
        int periode = choixPeriodeVisee.getValue();
        try {
            if (service.existeDoublon(clientChoisi, choixTypeMandat.getValue(), periode) && !confirmerDoublon()) {
                return;
            }
            Dossier dossier = new Dossier(clientChoisi, choixTypeMandat.getValue(), lireDateOuverture(), periode,
                    zoneDescription.getText(), session.getUtilisateur().id());
            Dossier cree = service.creerDossier(session, dossier);
            String message = "Le dossier " + cree.getNumero() + " a été créé.";
            afficherAlerte(Alert.AlertType.INFORMATION, messageSuite == null ? message : message + "\n\n" + messageSuite);
            stageUtilise.close();
        } catch (SessionExpireeException exception) {
            afficherAlerte(Alert.AlertType.WARNING, exception.getMessage());
        } catch (AccesRefuseException exception) {
            afficherAlerte(Alert.AlertType.ERROR, exception.getMessage());
        } catch (DonneesException exception) {
            // Les données restent dans le formulaire et rien n'a été enregistré.
            afficherAlerte(Alert.AlertType.ERROR, exception.getMessage());
        }
    }

    /** Avertit qu'un dossier actif du même type existe déjà (scénario 5a). */
    private boolean confirmerDoublon() {
        ButtonType continuer = new ButtonType("Continuer", ButtonBar.ButtonData.OK_DONE);
        ButtonType annuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        return confirmer("Un dossier actif du même type de mandat existe déjà pour ce client et cette période.\n"
                + "Voulez-vous quand même créer un nouveau dossier ?", continuer, annuler);
    }

    @FXML
    private void annuler() {
        if (confirmerAbandon()) {
            stageUtilise.close();
        }
    }

    /** @return {@code true} si on peut fermer : rien n'a été saisi ou la personne confirme l'abandon (scénario *a). */
    private boolean confirmerAbandon() {
        if (!aDesDonneesSaisies()) {
            return true;
        }
        return confirmer("Abandonner la création du dossier ? Les informations saisies seront perdues.",
                ButtonType.YES, ButtonType.NO);
    }

    private boolean aDesDonneesSaisies() {
        return clientChoisi != null
                || !champRechercheClient.getText().isBlank()
                || choixTypeMandat.getValue() != null
                || !zoneDescription.getText().isBlank();
    }

    @FXML
    private void nouveauClient() {
        afficherAlerte(Alert.AlertType.INFORMATION,
                "Le cas d'utilisation « Créer un client » n'est pas encore disponible.");
    }

    @FXML
    private void ajouterMandat() {
        afficherAlerte(Alert.AlertType.INFORMATION,
                "Le cas d'utilisation « Créer le mandat » n'est pas encore disponible.");
    }

    @FXML
    private void reessayer() {
        chargerNumeroPrevu();
        if (clientChoisi != null) {
            choisirClient(clientChoisi);
        }
        mettreAJourBoutons();
    }

    // ------------------------------------------------------------------ bandeau d'erreur

    private void afficherBandeau(String message) {
        messageBandeau.setText(message + " Voulez-vous réessayer ?");
        bandeauErreur.setVisible(true);
        bandeauErreur.setManaged(true);
    }

    private void masquerBandeau() {
        bandeauErreur.setVisible(false);
        bandeauErreur.setManaged(false);
    }
}
