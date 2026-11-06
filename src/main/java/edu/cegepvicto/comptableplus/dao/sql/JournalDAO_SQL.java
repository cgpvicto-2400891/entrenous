package edu.cegepvicto.comptableplus.dao.sql;

import edu.cegepvicto.comptableplus.dao.DonneesException;
import edu.cegepvicto.comptableplus.dao.JournalDAO;
import edu.cegepvicto.comptableplus.modele.ActionJournal;
import edu.cegepvicto.comptableplus.modele.EntreeJournal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Implémentation JDBC de {@link JournalDAO}. */
public class JournalDAO_SQL implements JournalDAO {

    private static final String REQUETE_INSERTION =
            "INSERT INTO Journal (date_heure, utilisateur_id, action, dossier_id) VALUES (?, ?, ?, ?)";

    private final Logger journaliseur = Logger.getLogger(getClass().getName());
    private final ConnexionBD connexionBD;

    /** @param connexionBD les paramètres de connexion à la base de données. */
    public JournalDAO_SQL(ConnexionBD connexionBD) {
        this.connexionBD = connexionBD;
    }

    @Override
    public void consigner(EntreeJournal entree) throws DonneesException {
        try (Connection connexion = connexionBD.ouvrir()) {
            consigner(connexion, entree);
        } catch (SQLException exception) {
            journaliseur.log(Level.SEVERE, "Écriture au journal impossible.", exception);
            throw new DonneesException("L'action n'a pas pu être consignée au journal.", exception);
        }
    }

    /**
     * Ajoute une entrée dans une connexion déjà ouverte, pour la joindre à une transaction en cours
     * (utilisé par {@link DossierDAO_SQL}).
     * @param connexion la connexion (et la transaction) à utiliser.
     * @param entree    l'action à consigner.
     * @throws SQLException si l'insertion échoue.
     */
    void consigner(Connection connexion, EntreeJournal entree) throws SQLException {
        try (PreparedStatement requete = connexion.prepareStatement(REQUETE_INSERTION)) {
            requete.setTimestamp(1, Timestamp.valueOf(entree.dateHeure()));
            requete.setInt(2, entree.utilisateurId());
            requete.setString(3, entree.action().name());
            if (entree.dossierId() == null) {
                requete.setNull(4, Types.INTEGER);
            } else {
                requete.setInt(4, entree.dossierId());
            }
            requete.executeUpdate();
        }
    }

    @Override
    public List<EntreeJournal> chargerTout() throws DonneesException {
        String requeteSql = "SELECT date_heure, utilisateur_id, action, dossier_id FROM Journal ORDER BY id";
        try (Connection connexion = connexionBD.ouvrir();
             PreparedStatement requete = connexion.prepareStatement(requeteSql);
             ResultSet resultats = requete.executeQuery()) {
            List<EntreeJournal> entrees = new ArrayList<>();
            while (resultats.next()) {
                int dossier = resultats.getInt("dossier_id");
                Integer dossierId = resultats.wasNull() ? null : dossier;
                entrees.add(new EntreeJournal(resultats.getTimestamp("date_heure").toLocalDateTime(),
                        resultats.getInt("utilisateur_id"),
                        ActionJournal.valueOf(resultats.getString("action")), dossierId));
            }
            return entrees;
        } catch (SQLException exception) {
            journaliseur.log(Level.SEVERE, "Lecture du journal impossible.", exception);
            throw new DonneesException("Le journal est inaccessible pour le moment.", exception);
        }
    }
}
