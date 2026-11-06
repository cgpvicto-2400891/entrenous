package edu.cegepvicto.comptableplus.modele;

/**
 * Type de mandat confié au cabinet (tenue de livres, déclaration de revenus, etc.).
 *
 * @param id                      identifiant du type de mandat.
 * @param nom                     nom affiché à l'écran.
 * @param descriptionObligatoire  {@code true} si le dossier doit alors avoir une description.
 */
public record TypeMandat(int id, String nom, boolean descriptionObligatoire) {

    @Override
    public String toString() {
        return nom;
    }
}
