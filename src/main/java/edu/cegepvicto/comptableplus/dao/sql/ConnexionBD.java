package edu.cegepvicto.comptableplus.dao.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Paramètres de connexion à la base de données. Les valeurs ne sont jamais écrites dans le code :
 * elles viennent de la configuration de l'environnement (voir {@code LocalisateurServices}).
 *
 * @param url         adresse JDBC, par exemple {@code jdbc:mysql://127.0.0.1:3306/comptables}.
 * @param utilisateur nom de la personne utilisatrice de la base de données.
 * @param motDePasse  mot de passe (ne doit jamais être consigné ni affiché).
 */
public record ConnexionBD(String url, String utilisateur, String motDePasse) {

    /**
     * Ouvre une nouvelle connexion. L'appelant la ferme (try-with-resources).
     * @return la connexion ouverte.
     * @throws SQLException si la base de données est inaccessible.
     */
    public Connection ouvrir() throws SQLException {
        return DriverManager.getConnection(url, utilisateur, motDePasse);
    }

    /** Le mot de passe est masqué pour qu'il n'apparaisse jamais dans un message ou un journal. */
    @Override
    public String toString() {
        return "ConnexionBD[url=" + url + ", utilisateur=" + utilisateur + "]";
    }
}
