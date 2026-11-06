package edu.cegepvicto.comptableplus.modele;

/** Client qui est une entreprise. */
public class ClientEntreprise extends Client {

    private final String nomEntreprise;
    private final String numeroEntreprise;
    private final String personneRessource;

    /**
     * Crée un client entreprise.
     * @param id                identifiant du client.
     * @param categorie         catégorie du client.
     * @param courriel          courriel du client.
     * @param telephone         téléphone du client.
     * @param adresse           adresse postale du client.
     * @param nomEntreprise     nom de l'entreprise.
     * @param numeroEntreprise  numéro d'entreprise, ou {@code null} s'il n'est pas connu.
     * @param personneRessource personne à joindre dans l'entreprise.
     */
    public ClientEntreprise(int id, CategorieClient categorie, String courriel, String telephone,
                            String adresse, String nomEntreprise, String numeroEntreprise,
                            String personneRessource) {
        super(id, categorie, courriel, telephone, adresse);
        this.nomEntreprise = nomEntreprise;
        this.numeroEntreprise = numeroEntreprise;
        this.personneRessource = personneRessource;
    }

    public String getNomEntreprise() {
        return nomEntreprise;
    }

    /** @return le numéro d'entreprise, ou {@code null} s'il n'est pas connu. */
    public String getNumeroEntreprise() {
        return numeroEntreprise;
    }

    public String getPersonneRessource() {
        return personneRessource;
    }

    @Override
    public String getNomAffichage() {
        return nomEntreprise;
    }
}
