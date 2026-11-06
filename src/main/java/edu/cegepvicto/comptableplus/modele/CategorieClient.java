package edu.cegepvicto.comptableplus.modele;

/**
 * Catégorie d'un client (table de référence). Elle détermine les types de mandat offerts.
 * Ajouter une catégorie revient à ajouter une ligne dans la base de données.
 *
 * @param id      identifiant de la catégorie.
 * @param code    code unique, par exemple {@code PARTICULIER} ou {@code ENTREPRISE}.
 * @param libelle texte affiché à l'écran.
 */
public record CategorieClient(int id, String code, String libelle) {

    @Override
    public String toString() {
        return libelle;
    }
}
