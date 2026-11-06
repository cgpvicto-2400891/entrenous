package edu.cegepvicto.comptableplus.modele;

/**
 * Personne utilisatrice connectée. L'identifiant vient de la base de données centrale.
 *
 * @param id   identifiant dans la base de données centrale.
 * @param nom  nom affiché à l'écran.
 * @param role rôle qui détermine les fonctionnalités accessibles.
 */
public record Utilisateur(int id, String nom, Role role) {
}
