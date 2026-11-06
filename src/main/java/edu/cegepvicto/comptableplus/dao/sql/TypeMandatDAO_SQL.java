package edu.cegepvicto.comptableplus.dao.sql;

import edu.cegepvicto.comptableplus.dao.DonneesException;
import edu.cegepvicto.comptableplus.dao.TypeMandatDAO;
import edu.cegepvicto.comptableplus.modele.CategorieClient;
import edu.cegepvicto.comptableplus.modele.TypeMandat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Implémentation JDBC de {@link TypeMandatDAO}. */
public class TypeMandatDAO_SQL implements TypeMandatDAO {

    private final Logger journaliseur = Logger.getLogger(getClass().getName());
    private final ConnexionBD connexionBD;

    /** @param connexionBD les paramètres de connexion à la base de données. */
    public TypeMandatDAO_SQL(ConnexionBD connexionBD) {
        this.connexionBD = connexionBD;
    }

    @Override
    public List<TypeMandat> chargerParCategorie(CategorieClient categorie) throws DonneesException {
        String requeteSql = "SELECT t.id, t.nom, t.description_obligatoire FROM TypeMandat t "
                + "JOIN TypeMandatCategorie tc ON tc.type_mandat_id = t.id "
                + "WHERE tc.categorie_id = ? ORDER BY t.nom";
        try (Connection connexion = connexionBD.ouvrir();
             PreparedStatement requete = connexion.prepareStatement(requeteSql)) {
            requete.setInt(1, categorie.id());
            try (ResultSet resultats = requete.executeQuery()) {
                List<TypeMandat> types = new ArrayList<>();
                while (resultats.next()) {
                    types.add(new TypeMandat(resultats.getInt("id"), resultats.getString("nom"),
                            resultats.getBoolean("description_obligatoire")));
                }
                return types;
            }
        } catch (SQLException exception) {
            journaliseur.log(Level.SEVERE, "Chargement des types de mandat impossible.", exception);
            throw new DonneesException("Les types de mandat sont inaccessibles pour le moment.", exception);
        }
    }
}
