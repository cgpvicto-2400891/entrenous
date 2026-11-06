package edu.cegepvicto.comptableplus.modele;

import java.time.Clock;
import java.time.Instant;

/**
 * Session d'une personne utilisatrice connectée. Elle expire à un moment précis.
 * La connexion elle-même est une précondition du CU et n'est pas traitée ici.
 */
public class Session {

    private final Utilisateur utilisateur;
    private final Instant expiration;
    private final Clock horloge;

    /**
     * Crée une session.
     * @param utilisateur la personne connectée.
     * @param expiration  le moment où la session cesse d'être valide.
     * @param horloge     l'horloge utilisée pour savoir si la session est expirée (remplaçable dans les tests).
     */
    public Session(Utilisateur utilisateur, Instant expiration, Clock horloge) {
        this.utilisateur = utilisateur;
        this.expiration = expiration;
        this.horloge = horloge;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    /** @return {@code true} si la session n'est plus valide. */
    public boolean estExpiree() {
        return !horloge.instant().isBefore(expiration);
    }
}
