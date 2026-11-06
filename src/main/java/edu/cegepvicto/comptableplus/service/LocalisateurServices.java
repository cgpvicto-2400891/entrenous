package edu.cegepvicto.comptableplus.service;

import edu.cegepvicto.comptableplus.dao.ClientDAO;
import edu.cegepvicto.comptableplus.dao.DossierDAO;
import edu.cegepvicto.comptableplus.dao.JournalDAO;
import edu.cegepvicto.comptableplus.dao.TypeMandatDAO;
import edu.cegepvicto.comptableplus.dao.memoire.ClientDAO_Memoire;
import edu.cegepvicto.comptableplus.dao.memoire.DepotMemoire;
import edu.cegepvicto.comptableplus.dao.memoire.DossierDAO_Memoire;
import edu.cegepvicto.comptableplus.dao.memoire.JournalDAO_Memoire;
import edu.cegepvicto.comptableplus.dao.memoire.TypeMandatDAO_Memoire;
import edu.cegepvicto.comptableplus.dao.sql.ClientDAO_SQL;
import edu.cegepvicto.comptableplus.dao.sql.ConnexionBD;
import edu.cegepvicto.comptableplus.dao.sql.DossierDAO_SQL;
import edu.cegepvicto.comptableplus.dao.sql.JournalDAO_SQL;
import edu.cegepvicto.comptableplus.dao.sql.TypeMandatDAO_SQL;
import edu.cegepvicto.comptableplus.modele.ValidateurDossier;

/**
 * Localisateur de services : point d'accès unique aux services de l'application. Il est créé une seule
 * fois au démarrage puis transmis aux fenêtres par le contexte ({@code DonneesSysteme}), ce qui évite
 * les classes statiques et le patron Singleton.
 * <p>
 * Le choix des DAO (base de données ou mémoire) se fait ici seulement : le reste du système ne le sait pas.
 */
public class LocalisateurServices {

    /** Variable d'environnement : adresse JDBC de la base de données (active le mode base de données). */
    public static final String VARIABLE_URL = "COMPTABLES_BD_URL";
    /** Variable d'environnement : nom de la personne utilisatrice de la base de données. */
    public static final String VARIABLE_UTILISATEUR = "COMPTABLES_BD_UTILISATEUR";
    /** Variable d'environnement : mot de passe de la base de données. */
    public static final String VARIABLE_MOT_DE_PASSE = "COMPTABLES_BD_MOT_DE_PASSE";

    private final ValidateurDossier validateur;
    private final ServiceAutorisation autorisation;
    private final ServiceDossier dossiers;

    /**
     * Assemble les services à partir des DAO choisis.
     * @param clientDAO     l'accès aux clients.
     * @param typeMandatDAO l'accès aux types de mandat.
     * @param dossierDAO    l'accès aux dossiers.
     * @param journalDAO    l'accès au journal.
     */
    public LocalisateurServices(ClientDAO clientDAO, TypeMandatDAO typeMandatDAO, DossierDAO dossierDAO,
                                JournalDAO journalDAO) {
        this.validateur = new ValidateurDossier();
        this.autorisation = new ServiceAutorisation(journalDAO);
        this.dossiers = new ServiceDossier(autorisation, clientDAO, typeMandatDAO, dossierDAO);
    }

    /**
     * Crée les services avec les DAO en mémoire et les données de démonstration.
     * @return le localisateur en mode mémoire.
     */
    public static LocalisateurServices enMemoire() {
        return enMemoire(DepotMemoire.avecDonneesDemo());
    }

    /**
     * Crée les services avec les DAO en mémoire sur un dépôt donné.
     * @param depot le dépôt de données partagé par les DAO.
     * @return le localisateur en mode mémoire.
     */
    public static LocalisateurServices enMemoire(DepotMemoire depot) {
        return new LocalisateurServices(new ClientDAO_Memoire(depot), new TypeMandatDAO_Memoire(depot),
                new DossierDAO_Memoire(depot), new JournalDAO_Memoire(depot));
    }

    /**
     * Crée les services avec les DAO JDBC.
     * @param connexionBD les paramètres de connexion à la base de données.
     * @return le localisateur en mode base de données.
     */
    public static LocalisateurServices avecBaseDeDonnees(ConnexionBD connexionBD) {
        return new LocalisateurServices(new ClientDAO_SQL(connexionBD), new TypeMandatDAO_SQL(connexionBD),
                new DossierDAO_SQL(connexionBD), new JournalDAO_SQL(connexionBD));
    }

    /**
     * Choisit le mode selon l'environnement : si {@value #VARIABLE_URL} est définie, la base de données
     * est utilisée; sinon, l'application démarre avec les données de démonstration en mémoire.
     * Les identifiants ne sont jamais écrits dans le code ni dans le dépôt Git.
     * @return le localisateur configuré.
     */
    public static LocalisateurServices depuisEnvironnement() {
        String url = System.getenv(VARIABLE_URL);
        if (url == null || url.isBlank()) {
            return enMemoire();
        }
        String utilisateur = System.getenv(VARIABLE_UTILISATEUR);
        String motDePasse = System.getenv(VARIABLE_MOT_DE_PASSE);
        return avecBaseDeDonnees(new ConnexionBD(url, utilisateur == null ? "" : utilisateur,
                motDePasse == null ? "" : motDePasse));
    }

    /** @return les règles de validation d'un dossier. */
    public ValidateurDossier validateur() {
        return validateur;
    }

    /** @return le service qui vérifie les droits d'accès. */
    public ServiceAutorisation autorisation() {
        return autorisation;
    }

    /** @return le service des opérations sur les dossiers. */
    public ServiceDossier dossiers() {
        return dossiers;
    }
}
