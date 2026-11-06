package edu.cegepvicto.comptableplus.dao.sql;

import edu.cegepvicto.comptableplus.dao.ClientDAO;
import edu.cegepvicto.comptableplus.dao.DonneesException;
import edu.cegepvicto.comptableplus.modele.CategorieClient;
import edu.cegepvicto.comptableplus.modele.Client;
import edu.cegepvicto.comptableplus.modele.ClientEntreprise;
import edu.cegepvicto.comptableplus.modele.ClientParticulier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Implémentation JDBC de {@link ClientDAO}. Toutes les requêtes sont préparées. */
public class ClientDAO_SQL implements ClientDAO {

    private static final int NOMBRE_MAX_RESULTATS = 20;
    private static final char CARACTERE_ECHAPPEMENT = '!';
    private static final String NOM_AFFICHAGE = "CONCAT_WS(' ', cp.prenom, cp.nom, ce.nom_entreprise)";

    private final Logger journaliseur = Logger.getLogger(getClass().getName());
    private final ConnexionBD connexionBD;

    /** @param connexionBD les paramètres de connexion à la base de données. */
    public ClientDAO_SQL(ConnexionBD connexionBD) {
        this.connexionBD = connexionBD;
    }

    @Override
    public List<Client> rechercherParNom(String texte) throws DonneesException {
        String recherche = texte == null ? "" : texte.strip();
        if (recherche.isEmpty()) {
            return List.of();
        }
        String requeteSql = "SELECT c.id, c.courriel, c.telephone, c.adresse, "
                + "cc.id AS categorie_id, cc.code, cc.libelle, "
                + "cp.prenom, cp.nom, ce.nom_entreprise, ce.numero_entreprise, ce.personne_ressource, "
                + NOM_AFFICHAGE + " AS nom_affichage "
                + "FROM Client c "
                + "JOIN CategorieClient cc ON cc.id = c.categorie_id "
                + "LEFT JOIN ClientParticulier cp ON cp.client_id = c.id "
                + "LEFT JOIN ClientEntreprise ce ON ce.client_id = c.id "
                + "WHERE LOWER(" + NOM_AFFICHAGE + ") LIKE ? ESCAPE '" + CARACTERE_ECHAPPEMENT + "' "
                + "ORDER BY nom_affichage LIMIT " + NOMBRE_MAX_RESULTATS;
        try (Connection connexion = connexionBD.ouvrir();
             PreparedStatement requete = connexion.prepareStatement(requeteSql)) {
            requete.setString(1, "%" + echapper(recherche.toLowerCase(Locale.ROOT)) + "%");
            try (ResultSet resultats = requete.executeQuery()) {
                List<Client> clients = new ArrayList<>();
                while (resultats.next()) {
                    clients.add(lireClient(resultats));
                }
                return clients;
            }
        } catch (SQLException exception) {
            journaliseur.log(Level.SEVERE, "Recherche de clients impossible.", exception);
            throw new DonneesException("La recherche de clients est impossible pour le moment.", exception);
        }
    }

    /** Les caractères % et _ saisis par la personne sont cherchés tels quels, pas comme jokers. */
    private String echapper(String texte) {
        return texte.replace(String.valueOf(CARACTERE_ECHAPPEMENT), CARACTERE_ECHAPPEMENT + "" + CARACTERE_ECHAPPEMENT)
                .replace("%", CARACTERE_ECHAPPEMENT + "%")
                .replace("_", CARACTERE_ECHAPPEMENT + "_");
    }

    private Client lireClient(ResultSet resultats) throws SQLException {
        CategorieClient categorie = new CategorieClient(resultats.getInt("categorie_id"),
                resultats.getString("code"), resultats.getString("libelle"));
        int id = resultats.getInt("id");
        String courriel = resultats.getString("courriel");
        String telephone = resultats.getString("telephone");
        String adresse = resultats.getString("adresse");
        String prenom = resultats.getString("prenom");
        if (prenom != null) {
            return new ClientParticulier(id, categorie, courriel, telephone, adresse, prenom,
                    resultats.getString("nom"));
        }
        return new ClientEntreprise(id, categorie, courriel, telephone, adresse,
                resultats.getString("nom_entreprise"), resultats.getString("numero_entreprise"),
                resultats.getString("personne_ressource"));
    }
}
