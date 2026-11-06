package edu.cegepvicto.comptableplus.dao;

import edu.cegepvicto.comptableplus.modele.Client;

import java.util.List;

/** Accès aux clients enregistrés. */
public interface ClientDAO {

    /**
     * Recherche les clients dont le nom (ou le nom de l'entreprise) contient le texte.
     * La recherche ignore la casse.
     * @param texte le texte recherché; un texte vide retourne une liste vide.
     * @return les clients trouvés, triés par nom.
     * @throws DonneesException si les données ne sont pas accessibles.
     */
    List<Client> rechercherParNom(String texte) throws DonneesException;
}
