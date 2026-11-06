package edu.cegepvicto.comptableplus.modele;

/**
 * Statut d'un dossier client. Un dossier n'est jamais supprimé (conservation de 7 ans) :
 * il passe de {@link #ACTIF} à {@link #ARCHIVE}.
 */
public enum StatutDossier {
    ACTIF("Actif"),
    ARCHIVE("Archivé");

    private final String libelle;

    StatutDossier(String libelle) {
        this.libelle = libelle;
    }

    /** @return le texte affiché et enregistré dans la base de données. */
    public String getLibelle() {
        return libelle;
    }

    /**
     * Retrouve un statut à partir de son libellé.
     * @param libelle le libellé enregistré (« Actif » ou « Archivé »).
     * @return le statut correspondant.
     * @throws IllegalArgumentException si le libellé est inconnu.
     */
    public static StatutDossier depuisLibelle(String libelle) {
        for (StatutDossier statut : values()) {
            if (statut.libelle.equals(libelle)) {
                return statut;
            }
        }
        throw new IllegalArgumentException("Statut de dossier inconnu : " + libelle);
    }

    @Override
    public String toString() {
        return libelle;
    }
}
