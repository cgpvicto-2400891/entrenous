package edu.cegepvicto.comptableplus.dao;

import edu.cegepvicto.comptableplus.modele.Client;
import edu.cegepvicto.comptableplus.modele.Dossier;
import edu.cegepvicto.comptableplus.modele.TypeMandat;

/**
 * Accès aux dossiers. Il n'existe aucune opération de suppression : un dossier est archivé,
 * jamais supprimé (conservation de 7 ans).
 */
public interface DossierDAO {

    /**
     * Donne le numéro que recevrait le prochain dossier de l'année. Ce numéro est indicatif :
     * le numéro définitif est attribué par {@link #creer(Dossier)}.
     * @param annee l'année du numéro (format AAAA-NNNN).
     * @return le numéro prévu, par exemple {@code 2026-0053}.
     * @throws DonneesException si les données ne sont pas accessibles.
     */
    String prochainNumero(int annee) throws DonneesException;

    /**
     * Compte les dossiers d'un client.
     * @param client le client.
     * @return le nombre de dossiers, tous statuts confondus.
     * @throws DonneesException si les données ne sont pas accessibles.
     */
    int compterParClient(Client client) throws DonneesException;

    /**
     * Indique si un dossier actif existe déjà pour le même client, le même type de mandat et la même période.
     * @param client       le client.
     * @param typeMandat   le type de mandat.
     * @param periodeVisee l'année financière visée.
     * @return {@code true} si un tel dossier actif existe.
     * @throws DonneesException si les données ne sont pas accessibles.
     */
    boolean existeDossierActif(Client client, TypeMandat typeMandat, int periodeVisee) throws DonneesException;

    /**
     * Enregistre un nouveau dossier, lui attribue un numéro unique (AAAA-NNNN) et consigne la création
     * au journal. Tout se fait en une seule opération : en cas d'échec, rien n'est enregistré.
     * @param dossier le dossier à enregistrer (sans numéro).
     * @return le dossier enregistré, avec son identifiant et son numéro.
     * @throws DonneesException si l'enregistrement échoue.
     */
    Dossier creer(Dossier dossier) throws DonneesException;
}
