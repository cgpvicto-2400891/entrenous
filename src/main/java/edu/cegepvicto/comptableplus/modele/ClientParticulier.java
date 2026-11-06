package edu.cegepvicto.comptableplus.modele;

/** Client qui est une personne (un particulier). */
public class ClientParticulier extends Client {

    private final String prenom;
    private final String nom;

    /**
     * Crée un client particulier.
     * @param id        identifiant du client.
     * @param categorie catégorie du client.
     * @param courriel  courriel du client.
     * @param telephone téléphone du client.
     * @param adresse   adresse postale du client.
     * @param prenom    prénom de la personne.
     * @param nom       nom de famille de la personne.
     */
    public ClientParticulier(int id, CategorieClient categorie, String courriel, String telephone,
                             String adresse, String prenom, String nom) {
        super(id, categorie, courriel, telephone, adresse);
        this.prenom = prenom;
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getNom() {
        return nom;
    }

    @Override
    public String getNomAffichage() {
        return prenom + " " + nom;
    }
}
