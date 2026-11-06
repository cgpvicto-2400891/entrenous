package edu.cegepvicto.comptableplus.modele;

import java.time.LocalDateTime;

/**
 * Trace d'une action : qui l'a faite, quand et sur quel dossier.
 *
 * @param dateHeure     moment de l'action.
 * @param utilisateurId identifiant de la personne (base centrale).
 * @param action        action effectuée.
 * @param dossierId     identifiant du dossier visé, ou {@code null} si aucun dossier n'est concerné.
 */
public record EntreeJournal(LocalDateTime dateHeure, int utilisateurId, ActionJournal action, Integer dossierId) {
}
