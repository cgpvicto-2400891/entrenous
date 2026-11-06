package edu.cegepvicto.comptableplus.service;

import edu.cegepvicto.comptableplus.dao.ClientDAO;
import edu.cegepvicto.comptableplus.dao.DonneesException;
import edu.cegepvicto.comptableplus.dao.DossierDAO;
import edu.cegepvicto.comptableplus.dao.TypeMandatDAO;
import edu.cegepvicto.comptableplus.modele.Client;
import edu.cegepvicto.comptableplus.modele.Dossier;
import edu.cegepvicto.comptableplus.modele.Session;
import edu.cegepvicto.comptableplus.modele.TypeMandat;

import java.time.Year;
import java.util.List;

/**
 * Opérations du cas d'utilisation CU-001-001 « Créer un dossier client ».
 * Les fenêtres passent par ce service et ne connaissent pas les DAO (faible couplage).
 */
public class ServiceDossier {

    private final ServiceAutorisation autorisation;
    private final ClientDAO clientDAO;
    private final TypeMandatDAO typeMandatDAO;
    private final DossierDAO dossierDAO;

    /**
     * @param autorisation  le service qui vérifie les droits d'accès.
     * @param clientDAO     l'accès aux clients.
     * @param typeMandatDAO l'accès aux types de mandat.
     * @param dossierDAO    l'accès aux dossiers.
     */
    public ServiceDossier(ServiceAutorisation autorisation, ClientDAO clientDAO, TypeMandatDAO typeMandatDAO,
                          DossierDAO dossierDAO) {
        this.autorisation = autorisation;
        this.clientDAO = clientDAO;
        this.typeMandatDAO = typeMandatDAO;
        this.dossierDAO = dossierDAO;
    }

    /**
     * Donne le numéro que recevrait un dossier créé maintenant (indicatif, le définitif est attribué à la création).
     * @return le numéro prévu, au format AAAA-NNNN.
     * @throws DonneesException si les données ne sont pas accessibles.
     */
    public String numeroPrevu() throws DonneesException {
        return dossierDAO.prochainNumero(Year.now().getValue());
    }

    /**
     * Recherche des clients par leur nom.
     * @param texte le texte recherché.
     * @return les clients trouvés.
     * @throws DonneesException si les données ne sont pas accessibles.
     */
    public List<Client> rechercherClients(String texte) throws DonneesException {
        return clientDAO.rechercherParNom(texte);
    }

    /**
     * Donne les types de mandat offerts à un client, selon sa catégorie.
     * @param client le client choisi.
     * @return les types de mandat offerts.
     * @throws DonneesException si les données ne sont pas accessibles.
     */
    public List<TypeMandat> typesMandatOffertsA(Client client) throws DonneesException {
        return typeMandatDAO.chargerParCategorie(client.getCategorie());
    }

    /**
     * Compte les dossiers d'un client.
     * @param client le client.
     * @return le nombre de dossiers existants.
     * @throws DonneesException si les données ne sont pas accessibles.
     */
    public int nombreDossiers(Client client) throws DonneesException {
        return dossierDAO.compterParClient(client);
    }

    /**
     * Indique si un dossier actif existe déjà pour le même client, type de mandat et période.
     * @param client       le client.
     * @param typeMandat   le type de mandat.
     * @param periodeVisee l'année financière visée.
     * @return {@code true} si un doublon existe (la personne décide alors de continuer ou non).
     * @throws DonneesException si les données ne sont pas accessibles.
     */
    public boolean existeDoublon(Client client, TypeMandat typeMandat, int periodeVisee) throws DonneesException {
        return dossierDAO.existeDossierActif(client, typeMandat, periodeVisee);
    }

    /**
     * Crée le dossier au nom du gestionnaire connecté. Si l'enregistrement échoue, aucun dossier
     * incomplet n'est conservé.
     * @param session la session de la personne connectée.
     * @param dossier le dossier validé, sans numéro.
     * @return le dossier enregistré, avec son numéro.
     * @throws SessionExpireeException  si la session est expirée.
     * @throws AccesRefuseException     si la personne n'est pas gestionnaire.
     * @throws DonneesException         si l'enregistrement échoue.
     * @throws IllegalArgumentException si le dossier n'appartient pas au gestionnaire connecté.
     */
    public Dossier creerDossier(Session session, Dossier dossier) throws AccesRefuseException, DonneesException {
        autorisation.verifierCreationDossier(session);
        if (dossier.getGestionnaireId() != session.getUtilisateur().id()) {
            throw new IllegalArgumentException("Le dossier doit être associé au gestionnaire connecté.");
        }
        return dossierDAO.creer(dossier);
    }
}
