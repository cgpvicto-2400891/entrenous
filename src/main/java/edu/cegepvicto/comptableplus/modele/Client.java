package edu.cegepvicto.comptableplus.modele;

/**
 * Personne cliente d'un cabinet : un particulier ou une entreprise.
 * Les coordonnées sont communes, le nom dépend du type de client (voir les sous-classes).
 */
public abstract class Client {

    private final int id;
    private final CategorieClient categorie;
    private final String courriel;
    private final String telephone;
    private final String adresse;

    /**
     * Crée un client.
     * @param id        identifiant du client.
     * @param categorie catégorie du client (jamais nulle).
     * @param courriel  courriel du client.
     * @param telephone téléphone du client.
     * @param adresse   adresse postale du client.
     */
    protected Client(int id, CategorieClient categorie, String courriel, String telephone, String adresse) {
        if (categorie == null) {
            throw new IllegalArgumentException("Un client doit avoir une catégorie.");
        }
        this.id = id;
        this.categorie = categorie;
        this.courriel = courriel;
        this.telephone = telephone;
        this.adresse = adresse;
    }

    /** @return le nom à afficher dans l'interface (nom complet ou nom de l'entreprise). */
    public abstract String getNomAffichage();

    public int getId() {
        return id;
    }

    public CategorieClient getCategorie() {
        return categorie;
    }

    public String getCourriel() {
        return courriel;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    /** @return les coordonnées sur une ligne (courriel, téléphone, adresse). */
    public String getCoordonnees() {
        return courriel + " · " + telephone + " · " + adresse;
    }

    @Override
    public String toString() {
        return getNomAffichage() + " (" + categorie.libelle() + ")";
    }
}
