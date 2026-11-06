package edu.cegepvicto.comptableplus.dao.sql;

import edu.cegepvicto.comptableplus.dao.DonneesException;
import edu.cegepvicto.comptableplus.dao.DossierDAO;
import edu.cegepvicto.comptableplus.modele.ActionJournal;
import edu.cegepvicto.comptableplus.modele.Client;
import edu.cegepvicto.comptableplus.modele.Dossier;
import edu.cegepvicto.comptableplus.modele.EntreeJournal;
import edu.cegepvicto.comptableplus.modele.StatutDossier;
import edu.cegepvicto.comptableplus.modele.TypeMandat;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implémentation JDBC de {@link DossierDAO}.
 * <p>
 * Le numéro est calculé puis inséré dans une transaction. Si deux gestionnaires obtiennent le même
 * numéro en même temps, la contrainte UNIQUE refuse le second : la transaction est annulée et la
 * création est reprise dans une nouvelle transaction avec un nouveau numéro.
 */
public class DossierDAO_SQL implements DossierDAO {

    private static final int NOMBRE_MAX_TENTATIVES = 5;
    private static final String FORMAT_NUMERO = "%d-%04d";

    private final Logger journaliseur = Logger.getLogger(getClass().getName());
    private final ConnexionBD connexionBD;
    private final JournalDAO_SQL journalDAO;

    /** @param connexionBD les paramètres de connexion à la base de données. */
    public DossierDAO_SQL(ConnexionBD connexionBD) {
        this.connexionBD = connexionBD;
        this.journalDAO = new JournalDAO_SQL(connexionBD);
    }

    @Override
    public String prochainNumero(int annee) throws DonneesException {
        try (Connection connexion = connexionBD.ouvrir()) {
            return String.format(FORMAT_NUMERO, annee, derniereSequence(connexion, annee) + 1);
        } catch (SQLException exception) {
            journaliseur.log(Level.SEVERE, "Calcul du prochain numéro de dossier impossible.", exception);
            throw new DonneesException("Le numéro de dossier est inaccessible pour le moment.", exception);
        }
    }

    @Override
    public int compterParClient(Client client) throws DonneesException {
        String requeteSql = "SELECT COUNT(*) FROM Dossier WHERE client_id = ?";
        try (Connection connexion = connexionBD.ouvrir();
             PreparedStatement requete = connexion.prepareStatement(requeteSql)) {
            requete.setInt(1, client.getId());
            try (ResultSet resultats = requete.executeQuery()) {
                resultats.next();
                return resultats.getInt(1);
            }
        } catch (SQLException exception) {
            journaliseur.log(Level.SEVERE, "Comptage des dossiers d'un client impossible.", exception);
            throw new DonneesException("Les dossiers du client sont inaccessibles pour le moment.", exception);
        }
    }

    @Override
    public boolean existeDossierActif(Client client, TypeMandat typeMandat, int periodeVisee)
            throws DonneesException {
        String requeteSql = "SELECT COUNT(*) FROM Dossier "
                + "WHERE client_id = ? AND type_mandat_id = ? AND periode_visee = ? AND statut = ?";
        try (Connection connexion = connexionBD.ouvrir();
             PreparedStatement requete = connexion.prepareStatement(requeteSql)) {
            requete.setInt(1, client.getId());
            requete.setInt(2, typeMandat.id());
            requete.setInt(3, periodeVisee);
            requete.setString(4, StatutDossier.ACTIF.getLibelle());
            try (ResultSet resultats = requete.executeQuery()) {
                resultats.next();
                return resultats.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            journaliseur.log(Level.SEVERE, "Recherche d'un dossier en double impossible.", exception);
            throw new DonneesException("La vérification des dossiers existants est impossible pour le moment.",
                    exception);
        }
    }

    @Override
    public Dossier creer(Dossier dossier) throws DonneesException {
        SQLException derniereErreur = null;
        for (int tentative = 1; tentative <= NOMBRE_MAX_TENTATIVES; tentative++) {
            try {
                return creerDansUneTransaction(dossier);
            } catch (SQLIntegrityConstraintViolationException exception) {
                derniereErreur = exception;   // numéro déjà pris : nouvelle tentative, nouveau numéro
            } catch (SQLException exception) {
                derniereErreur = exception;
                break;
            }
        }
        journaliseur.log(Level.SEVERE, "Création du dossier impossible.", derniereErreur);
        throw new DonneesException("Le dossier n'a pas pu être créé. Veuillez réessayer.", derniereErreur);
    }

    /** Insère le dossier et son entrée de journal : tout est enregistré (commit) ou rien (rollback). */
    private Dossier creerDansUneTransaction(Dossier dossier) throws SQLException {
        try (Connection connexion = connexionBD.ouvrir()) {
            connexion.setAutoCommit(false);
            try {
                int annee = LocalDateTime.now().getYear();
                String numero = String.format(FORMAT_NUMERO, annee, derniereSequence(connexion, annee) + 1);
                int id = insererDossier(connexion, dossier, numero);
                journalDAO.consigner(connexion, new EntreeJournal(LocalDateTime.now(),
                        dossier.getGestionnaireId(), ActionJournal.CREATION_DOSSIER, id));
                connexion.commit();
                return dossier.avecIdentification(id, numero);
            } catch (SQLException exception) {
                connexion.rollback();
                throw exception;
            }
        }
    }

    private int insererDossier(Connection connexion, Dossier dossier, String numero) throws SQLException {
        String requeteSql = "INSERT INTO Dossier (numero, date_ouverture, statut, periode_visee, description, "
                + "client_id, type_mandat_id, gestionnaire_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement requete = connexion.prepareStatement(requeteSql, Statement.RETURN_GENERATED_KEYS)) {
            requete.setString(1, numero);
            requete.setDate(2, Date.valueOf(dossier.getDateOuverture()));
            requete.setString(3, dossier.getStatut().getLibelle());
            requete.setInt(4, dossier.getPeriodeVisee());
            requete.setString(5, dossier.getDescription());
            requete.setInt(6, dossier.getClient().getId());
            requete.setInt(7, dossier.getTypeMandat().id());
            requete.setInt(8, dossier.getGestionnaireId());
            requete.executeUpdate();
            try (ResultSet cles = requete.getGeneratedKeys()) {
                cles.next();
                return cles.getInt(1);
            }
        }
    }

    /** Plus grande séquence NNNN utilisée pour l'année. Le format fixe permet de comparer les textes. */
    private int derniereSequence(Connection connexion, int annee) throws SQLException {
        String prefixe = annee + "-";
        try (PreparedStatement requete = connexion.prepareStatement(
                "SELECT MAX(numero) FROM Dossier WHERE numero LIKE ?")) {
            requete.setString(1, prefixe + "%");
            try (ResultSet resultats = requete.executeQuery()) {
                resultats.next();
                String maximum = resultats.getString(1);
                return maximum == null ? 0 : Integer.parseInt(maximum.substring(prefixe.length()));
            }
        }
    }
}
